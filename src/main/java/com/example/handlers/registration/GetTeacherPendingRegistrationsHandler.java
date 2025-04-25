package com.example.handlers.registration;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.models.CourseRegistration;
import com.example.services.DynamoDBService;
import com.example.utils.CorsHeaders;
import com.google.gson.Gson;
import java.util.List;

public class GetTeacherPendingRegistrationsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDBService dynamoDBService;
    private final Gson gson;

    public GetTeacherPendingRegistrationsHandler() {
        this.dynamoDBService = new DynamoDBService();
        this.gson = new Gson();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String teacherId = request.getPathParameters().get("teacherId");
            
            // First get all courses for this teacher
            List<String> courseIds = dynamoDBService.getCourseIdsByInstructor(teacherId);
            
            // Then get pending registrations for these courses
            List<CourseRegistration> registrations = dynamoDBService.getPendingRegistrationsByCourseIds(courseIds);

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody(gson.toJson(registrations));
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
} 