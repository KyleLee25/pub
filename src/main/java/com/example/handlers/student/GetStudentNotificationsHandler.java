package com.example.handlers.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GetStudentNotificationsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String studentId = input.getPathParameters().get("studentId");
            context.getLogger().log("Getting notifications for student: " + studentId);

            if (studentId == null || studentId.trim().isEmpty()) {
                context.getLogger().log("Error: studentId is required");
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"error\":\"studentId is required\"}");
            }

            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#studentId", "studentId");

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":studentId", AttributeValue.builder().s(studentId).build());

            String tableName = System.getenv("NOTIFICATIONS_TABLE");
            context.getLogger().log("Using table: " + tableName);

            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("StudentIdIndex")
                    .keyConditionExpression("#studentId = :studentId")
                    .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            context.getLogger().log("Query request: " + queryRequest.toString());
            QueryResponse response = dynamoDbClient.query(queryRequest);
            context.getLogger().log("Found " + response.items().size() + " notifications");

            List<Map<String, Object>> notifications = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                notifications.add(Map.of(
                        "id", item.get("id").s(),
                        "studentId", item.get("studentId").s(),
                        "type", item.get("type").s(),
                        "message", item.get("message").s(),
                        "read", Boolean.parseBoolean(item.get("read").s()),
                        "createdAt", item.get("createdAt").s()
                ));
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(objectMapper.writeValueAsString(notifications));

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 