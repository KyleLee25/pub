package com.example.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ListUsersResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.example.utils.CorsHeaders;

import java.util.List;

public class ListUsersHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final CognitoIdentityProviderClient cognitoClient;
    private final ObjectMapper objectMapper;
    private final String userPoolId;

    public ListUsersHandler() {
        this.cognitoClient = CognitoIdentityProviderClient.create();
        this.objectMapper = new ObjectMapper();
        this.userPoolId = System.getenv("USER_POOL_ID");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setHeaders(CorsHeaders.getCorsHeaders());

        try {
            ListUsersRequest listUsersRequest = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .build();

            ListUsersResponse result = cognitoClient.listUsers(listUsersRequest);
            List<UserType> users = result.users();

            ArrayNode usersArray = objectMapper.createArrayNode();
            for (UserType user : users) {
                ObjectNode userNode = objectMapper.createObjectNode();
                userNode.put("id", user.username());
                userNode.put("email", user.username());
                
                // Get user attributes
                user.attributes().forEach(attr -> {
                    if ("name".equals(attr.name())) {
                        userNode.put("name", attr.value());
                    } else if ("custom:role".equals(attr.name())) {
                        userNode.put("role", attr.value());
                    }
                });

                usersArray.add(userNode);
            }

            response.setStatusCode(200);
            response.setBody(objectMapper.writeValueAsString(usersArray));
        } catch (Exception e) {
            context.getLogger().log("Error listing users: " + e.getMessage());
            response.setStatusCode(500);
            response.setBody("{\"error\":\"Failed to list users\"}");
        }

        return response;
    }
} 