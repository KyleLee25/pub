package com.example.models;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.models.Course;
import com.example.services.DynamoDBService;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateCourseHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDBService dbService;
    private final ObjectMapper mapper;

    public UpdateCourseHandler() {
        this.dbService = new DynamoDBService();
        this.mapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            String courseId = input.getPathParameters().get("id");
            Course course = mapper.readValue(input.getBody(), Course.class);
            course.setId(courseId);
            course.setUpdatedAt(java.time.Instant.now().toString());

            Course updatedCourse = dbService.updateCourse(course);
            if (updatedCourse == null) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"message\": \"Failed to update course\"}");
            }

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody(mapper.writeValueAsString(updatedCourse));

        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"message\": \"Internal server error\"}");
        }
    }
}