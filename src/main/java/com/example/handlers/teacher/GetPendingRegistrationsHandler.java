package com.example.handlers.teacher;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.services.DynamoDBService;
import com.example.models.CourseRegistration;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class GetPendingRegistrationsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDBService dynamoDBService;
    private final ObjectMapper objectMapper;

    public GetPendingRegistrationsHandler() {
        this.dynamoDBService = new DynamoDBService();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String teacherId = input.getPathParameters().get("id");
            
            // First get all course IDs for this teacher
            List<String> courseIds = dynamoDBService.getCourseIdsByInstructor(teacherId);
            
            // Then get all pending registrations for these courses
            List<CourseRegistration> registrations = dynamoDBService.getPendingRegistrationsByCourseIds(courseIds);

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody(objectMapper.writeValueAsString(registrations));
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
} 