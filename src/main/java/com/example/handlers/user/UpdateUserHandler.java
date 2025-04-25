package com.example.handlers.user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UpdateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final CognitoIdentityProviderClient cognitoClient;
    private final ObjectMapper objectMapper;
    private final String userPoolId;

    public UpdateUserHandler() {
        this.cognitoClient = CognitoIdentityProviderClient.create();
        this.objectMapper = new ObjectMapper();
        this.userPoolId = System.getenv("USER_POOL_ID");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Starting user update handler");
        try {
            String currentEmail = input.getPathParameters().get("id");
            context.getLogger().log("Updating user with current email: " + currentEmail);

            // Find user by email
            ListUsersRequest listUsersRequest = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .filter("email = \"" + currentEmail + "\"")
                .limit(1)
                .build();

            ListUsersResponse listUsersResponse = cognitoClient.listUsers(listUsersRequest);
            if (listUsersResponse.users().isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(404)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"error\":\"User not found\"}");
            }

            String username = listUsersResponse.users().get(0).username();
            Map<String, String> updates = objectMapper.readValue(input.getBody(), Map.class);
            context.getLogger().log("Update data: " + updates);

            List<AttributeType> userAttributes = new ArrayList<>();
            
            // Handle email update
            if (updates.containsKey("email") && !updates.get("email").equals(currentEmail)) {
                userAttributes.add(AttributeType.builder()
                    .name("email")
                    .value(updates.get("email"))
                    .build());
                // Add email_verified attribute
                userAttributes.add(AttributeType.builder()
                    .name("email_verified")
                    .value("true")
                    .build());
            }

            // Handle other attributes
            if (updates.containsKey("name")) {
                userAttributes.add(AttributeType.builder()
                    .name("name")
                    .value(updates.get("name"))
                    .build());
            }
            if (updates.containsKey("role")) {
                userAttributes.add(AttributeType.builder()
                    .name("custom:role")
                    .value(updates.get("role"))
                    .build());
            }

            if (!userAttributes.isEmpty()) {
                AdminUpdateUserAttributesRequest updateRequest = AdminUpdateUserAttributesRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .userAttributes(userAttributes)
                    .build();

                context.getLogger().log("Sending update request: " + updateRequest);
                cognitoClient.adminUpdateUserAttributes(updateRequest);
            }

            // Return the updated user data
            Map<String, Object> responseData = Map.of(
                "id", updates.getOrDefault("email", currentEmail),
                "email", updates.getOrDefault("email", currentEmail),
                "name", updates.get("name"),
                "role", updates.get("role")
            );

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody(objectMapper.writeValueAsString(responseData));

        } catch (Exception e) {
            context.getLogger().log("Error updating user: " + e.getMessage());
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 