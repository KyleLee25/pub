package com.example.handlers.teacher;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.HashMap;

public class GetTeacherHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String id = input.getPathParameters().get("id");
            
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(id).build());

            GetItemRequest getItemRequest = GetItemRequest.builder()
                    .tableName(System.getenv("TEACHERS_TABLE"))
                    .key(key)
                    .build();

            GetItemResponse response = dynamoDbClient.getItem(getItemRequest);
            
            if (!response.hasItem()) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404)
                        .withHeaders(CorsHeaders.getCorsHeaders())
                        .withBody("{\"error\":\"Teacher not found\"}");
            }

            Map<String, AttributeValue> item = response.item();
            Map<String, Object> teacher = Map.of(
                    "id", item.get("id").s(),
                    "name", item.get("name").s(),
                    "email", item.get("email").s(),
                    "createdAt", item.get("createdAt").s()
            );

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(objectMapper.writeValueAsString(teacher));

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 