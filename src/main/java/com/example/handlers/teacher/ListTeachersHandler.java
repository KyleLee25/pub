package com.example.handlers.teacher;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListTeachersHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(System.getenv("TEACHERS_TABLE"))
                    .build();

            ScanResponse response = dynamoDbClient.scan(scanRequest);
            List<Map<String, Object>> teachers = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                teachers.add(Map.of(
                        "id", item.get("id").s(),
                        "name", item.get("name").s(),
                        "email", item.get("email").s(),
                        "createdAt", item.get("createdAt").s()
                ));
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(objectMapper.writeValueAsString(teachers));

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 