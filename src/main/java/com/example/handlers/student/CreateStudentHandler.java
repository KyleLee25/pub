package com.example.handlers.student;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.example.models.Student;
import com.example.services.DynamoDBService;
import com.example.utils.CorsHeaders;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import software.amazon.awssdk.regions.Region;
import java.util.stream.Collectors;

public class CreateStudentHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDBService dbService;
    private final ObjectMapper mapper;
    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;

    public CreateStudentHandler() {
        this.dbService = new DynamoDBService();
        this.mapper = new ObjectMapper();
        this.cognitoClient = CognitoIdentityProviderClient.builder()
            .region(Region.US_EAST_1)
            .build();
        this.userPoolId = System.getenv("USER_POOL_ID");
    }

    // Create a class to hold the request data
    private static class CreateStudentRequest {
        public String name;
        public String email;
        public String password;
        public String major;
        public int enrollmentYear;
        public int graduationYear;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Received request to create student");
        
        try {
            if (input.getBody() == null) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody("{\"message\": \"Invalid request body\"}");
            }

            // Parse the request
            CreateStudentRequest request = mapper.readValue(input.getBody(), CreateStudentRequest.class);
            String studentId = UUID.randomUUID().toString();

            try {
                // First, create the Cognito user
                Map<String, String> userAttributes = new HashMap<>();
                userAttributes.put("email", request.email);
                userAttributes.put("name", request.name);
                userAttributes.put("custom:role", "student");
                userAttributes.put("custom:studentId", studentId);

                AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(request.email)
                    .temporaryPassword(request.password)
                    .userAttributes(
                        userAttributes.entrySet().stream()
                            .map(entry -> AttributeType.builder()
                                .name(entry.getKey())
                                .value(entry.getValue())
                                .build())
                            .collect(Collectors.toList())
                    )
                    .messageAction(MessageActionType.SUPPRESS) // Suppress welcome email
                    .build();

                AdminCreateUserResponse cognitoResponse = cognitoClient.adminCreateUser(createUserRequest);

                // Set the permanent password
                AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(request.email)
                    .password(request.password)
                    .permanent(true)
                    .build();

                cognitoClient.adminSetUserPassword(setPasswordRequest);

                // Now create the student record in DynamoDB
                Student student = new Student();
                student.setId(studentId);
                student.setName(request.name);
                student.setEmail(request.email);
                student.setMajor(request.major);
                student.setEnrollmentYear(request.enrollmentYear);
                student.setGraduationYear(request.graduationYear);
                
                String currentTime = Instant.now().toString();
                student.setCreatedAt(currentTime);
                student.setUpdatedAt(currentTime);

                Student createdStudent = dbService.createStudent(student);
                if (createdStudent == null) {
                    // If DynamoDB creation fails, delete the Cognito user
                    cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                        .userPoolId(userPoolId)
                        .username(request.email)
                        .build());

                    throw new RuntimeException("Failed to create student in database");
                }

                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withHeaders(CorsHeaders.getCorsHeaders())
                    .withBody(mapper.writeValueAsString(createdStudent));

            } catch (Exception e) {
                logger.log("Error during user creation: " + e.getMessage());
                
                // If it's a Cognito error about invalid password, return a 400
                if (e instanceof InvalidPasswordException) {
                    return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withHeaders(CorsHeaders.getCorsHeaders())
                        .withBody("{\"message\": \"Password must be at least 8 characters long and contain uppercase, lowercase, numbers, and special characters\"}");
                }
                
                // If it's a username exists error, return a 409
                if (e instanceof UsernameExistsException) {
                    return new APIGatewayProxyResponseEvent()
                        .withStatusCode(409)
                        .withHeaders(CorsHeaders.getCorsHeaders())
                        .withBody("{\"message\": \"An account with this email already exists\"}");
                }

                throw e;
            }

        } catch (Exception e) {
            logger.log("ERROR: Error creating student: " + e.getMessage());
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withHeaders(CorsHeaders.getCorsHeaders())
                .withBody("{\"message\": \"Internal server error\"}");
        }
    }
} 