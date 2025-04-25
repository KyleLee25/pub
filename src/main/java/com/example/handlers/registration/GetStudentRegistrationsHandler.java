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

public class GetStudentRegistrationsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDBService dynamoDBService;
    private final Gson gson;

    public GetStudentRegistrationsHandler() {
        this.dynamoDBService = new DynamoDBService();
        this.gson = new Gson();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String studentId = request.getPathParameters().get("studentId");
            List<CourseRegistration> registrations = dynamoDBService.getStudentRegistrations(studentId);

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