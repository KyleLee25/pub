package com.example.handlers.registration;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.models.CourseRegistration;
import com.example.services.DynamoDBService;
import com.example.utils.CorsHeaders;
import com.google.gson.Gson;

public class CreateRegistrationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDBService dynamoDBService;
    private final Gson gson;

    public CreateRegistrationHandler() {
        this.dynamoDBService = new DynamoDBService();
        this.gson = new Gson();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            CourseRegistration registration = gson.fromJson(request.getBody(), CourseRegistration.class);
            CourseRegistration created = dynamoDBService.createRegistration(registration);

            if (created != null) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(gson.toJson(created));
            } else {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"message\": \"Error creating registration\"}");
            }
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(400)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }
} 