package com.example.handlers.user;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeleteUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;

    public DeleteUserHandler() {
        this.cognitoClient = CognitoIdentityProviderClient.create();
        this.userPoolId = System.getenv("USER_POOL_ID");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        context.getLogger().log("Starting user delete handler");
        try {
            String userEmail = input.getPathParameters().get("id");
            context.getLogger().log("Deleting user with email: " + userEmail);

            // First list users to find the one with matching email
            ListUsersRequest listUsersRequest = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .filter("email = \"" + userEmail + "\"")
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
            
            // Delete the user from Cognito
            AdminDeleteUserRequest deleteRequest = AdminDeleteUserRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .build();

            cognitoClient.adminDeleteUser(deleteRequest);

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"message\":\"User deleted successfully\"}");

        } catch (Exception e) {
            context.getLogger().log("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 