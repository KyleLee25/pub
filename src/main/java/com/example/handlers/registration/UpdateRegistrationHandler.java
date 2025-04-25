package com.example.handlers.registration;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.example.models.CourseRegistration;
import com.example.services.DynamoDBService;
import com.example.utils.CorsHeaders;
import com.google.gson.Gson;
import java.util.Map;

public class UpdateRegistrationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDBService dynamoDBService;
    private final Gson gson;

    public UpdateRegistrationHandler() {
        this.dynamoDBService = new DynamoDBService();
        this.gson = new Gson();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String registrationId = request.getPathParameters().get("id");
            Map<String, String> body = gson.fromJson(request.getBody(), Map.class);
            String status = body.get("status");

            if (!status.equals("approved") && !status.equals("rejected")) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"message\": \"Status must be either 'approved' or 'rejected'\"}");
            }

            CourseRegistration updated = dynamoDBService.updateRegistrationStatus(registrationId, status);

            if (updated != null) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(gson.toJson(updated));
            } else {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"message\": \"Error updating registration\"}");
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