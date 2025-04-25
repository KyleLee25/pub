package com.example.handlers.teacher;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminSetUserPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserResponse;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class CreateTeacherHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Parse request body
            Map<String, Object> requestBody = objectMapper.readValue(input.getBody(), Map.class);
            String name = (String) requestBody.get("name");
            String email = (String) requestBody.get("email");
            String password = (String) requestBody.get("password");

            // Create user in Cognito
            String userPoolId = System.getenv("USER_POOL_ID");
            AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .userAttributes(
                            AttributeType.builder().name("email").value(email).build(),
                            AttributeType.builder().name("name").value(name).build(),
                            AttributeType.builder().name("custom:role").value("teacher").build()
                    )
                    .build();

            AdminCreateUserResponse createUserResponse = cognitoClient.adminCreateUser(createUserRequest);
            String cognitoUserId = createUserResponse.user().username();

            // Set user password
            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
                    .password(password)
                    .permanent(true)
                    .build();

            cognitoClient.adminSetUserPassword(setPasswordRequest);

            // Save teacher in DynamoDB
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(cognitoUserId).build());
            item.put("name", AttributeValue.builder().s(name).build());
            item.put("email", AttributeValue.builder().s(email).build());
            item.put("createdAt", AttributeValue.builder().s(java.time.Instant.now().toString()).build());

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(System.getenv("TEACHERS_TABLE"))
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);

            // Return success response
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(objectMapper.writeValueAsString(Map.of(
                            "id", cognitoUserId,
                            "name", name,
                            "email", email
                    )));

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 