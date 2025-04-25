package com.example.handlers.teacher;

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

public class GetTeacherCoursesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String teacherId = input.getPathParameters().get("id");
            context.getLogger().log("Getting courses for teacher: " + teacherId);

            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#instructor", "instructor");

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":instructor", AttributeValue.builder().s(teacherId).build());

            String tableName = System.getenv("COURSES_TABLE");
            context.getLogger().log("Using table: " + tableName);

            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("InstructorIndex")
                    .keyConditionExpression("#instructor = :instructor")
                    .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

            context.getLogger().log("Query request: " + queryRequest.toString());
            QueryResponse response = dynamoDbClient.query(queryRequest);
            context.getLogger().log("Found " + response.items().size() + " courses");

            List<Map<String, Object>> courses = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                courses.add(Map.of(
                        "id", item.get("id").s(),
                        "name", item.get("name").s(),
                        "code", item.get("code").s(),
                        "description", item.get("description").s(),
                        "credits", item.get("credits").n(),
                        "semester", item.get("semester").s(),
                        "instructor", item.get("instructor").s(),
                        "createdAt", item.get("createdAt").s(),
                        "updatedAt", item.get("updatedAt").s()
                ));
            }

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(objectMapper.writeValueAsString(courses));

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