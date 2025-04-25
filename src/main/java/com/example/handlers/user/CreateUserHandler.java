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

public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final CognitoIdentityProviderClient cognitoClient;
    private final ObjectMapper objectMapper;
    private final String userPoolId;

    public CreateUserHandler() {
        this.cognitoClient = CognitoIdentityProviderClient.create();
        this.objectMapper = new ObjectMapper();
        this.userPoolId = System.getenv("USER_POOL_ID");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Starting user creation handler");
        try {
            Map<String, String> userData = objectMapper.readValue(input.getBody(), Map.class);
            String email = userData.get("email");
            String name = userData.get("name");
            String password = userData.get("password");
            String role = userData.get("role");

            context.getLogger().log("Creating user with email: " + email);

            // Create user attributes
            List<AttributeType> userAttributes = new ArrayList<>();
            userAttributes.add(AttributeType.builder()
                .name("email")
                .value(email)
                .build());
            userAttributes.add(AttributeType.builder()
                .name("email_verified")
                .value("true")
                .build());
            userAttributes.add(AttributeType.builder()
                .name("name")
                .value(name)
                .build());
            userAttributes.add(AttributeType.builder()
                .name("custom:role")
                .value(role)
                .build());

            // Create user in Cognito
            AdminCreateUserRequest createRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .temporaryPassword(password)
                .userAttributes(userAttributes)
                .messageAction(MessageActionType.SUPPRESS)
                .build();

            AdminCreateUserResponse createResponse = cognitoClient.adminCreateUser(createRequest);
            String userId = createResponse.user().username();

            // Set permanent password
            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(email)
                .password(password)
                .permanent(true)
                .build();

            cognitoClient.adminSetUserPassword(setPasswordRequest);

            // Return the created user data
            Map<String, Object> responseData = Map.of(
                "id", email,
                "email", email,
                "name", name,
                "role", role
            );

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody(objectMapper.writeValueAsString(responseData));

        } catch (UserNotFoundException e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(404)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"error\":\"User not found\"}");
        } catch (UsernameExistsException e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(409)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"error\":\"A user with this email already exists\"}");
        } catch (Exception e) {
            context.getLogger().log("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 