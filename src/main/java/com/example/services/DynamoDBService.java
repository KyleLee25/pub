package com.example.services;

import com.example.models.Student;
import com.example.models.Course;
import com.example.models.Grade;
import com.example.models.CourseRegistration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import java.net.URI;
import java.util.stream.Collectors;

public class DynamoDBService {
    private final DynamoDbClient client;
    private final String tableName;
    private final String studentsTable;
    private final String coursesTable;
    private final String gradesTable;
    private final String registrationsTable;

    public DynamoDBService(DynamoDbClient dynamoDbClient) {
        this.client = dynamoDbClient;
        this.tableName = System.getenv("TABLE_NAME");
        this.studentsTable = System.getenv("STUDENTS_TABLE");
        this.coursesTable = System.getenv("COURSES_TABLE");
        this.gradesTable = System.getenv("GRADES_TABLE");
        this.registrationsTable = System.getenv("REGISTRATIONS_TABLE");

        System.out.println("Initialized DynamoDB Service with tables:");
        System.out.println("Main table: " + this.tableName);
        System.out.println("Students table: " + this.studentsTable);
        System.out.println("Courses table: " + this.coursesTable);
        System.out.println("Grades table: " + this.gradesTable);
        System.out.println("Registrations table: " + this.registrationsTable);
    }

    public DynamoDBService() {
        boolean isOffline = Boolean.parseBoolean(System.getProperty("IS_OFFLINE", "false"));
        
        if (isOffline) {
            // Use DynamoDB Local
            this.client = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:8000"))
                .credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("dummy", "dummy")))
                .region(Region.US_EAST_1)
                .build();
        } else {
            // Use AWS DynamoDB
            this.client = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .build();
        }
        
        this.tableName = System.getenv("TABLE_NAME");
        this.studentsTable = System.getenv("STUDENTS_TABLE");
        this.coursesTable = System.getenv("COURSES_TABLE");
        this.gradesTable = System.getenv("GRADES_TABLE");
        this.registrationsTable = System.getenv("REGISTRATIONS_TABLE");

        System.out.println("Initialized DynamoDB Service with tables:");
        System.out.println("Main table: " + this.tableName);
        System.out.println("Students table: " + this.studentsTable);
        System.out.println("Courses table: " + this.coursesTable);
        System.out.println("Grades table: " + this.gradesTable);
        System.out.println("Registrations table: " + this.registrationsTable);
    }

   

    public boolean deleteItem(String id) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(id).build());

            DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

            DeleteItemResponse response = client.deleteItem(request);
            return response.sdkHttpResponse().isSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Student Methods
    public Student createStudent(Student student) {
        try {
            System.out.println("Creating student with data: " + student);
            System.out.println("Using table name: " + studentsTable);
            
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(student.getId()).build());
            item.put("name", AttributeValue.builder().s(student.getName()).build());
            item.put("email", AttributeValue.builder().s(student.getEmail()).build());
            item.put("major", AttributeValue.builder().s(student.getMajor()).build());
            item.put("enrollmentYear", AttributeValue.builder().n(String.valueOf(student.getEnrollmentYear())).build());
            item.put("graduationYear", AttributeValue.builder().n(String.valueOf(student.getGraduationYear())).build());
            item.put("createdAt", AttributeValue.builder().s(student.getCreatedAt()).build());
            item.put("updatedAt", AttributeValue.builder().s(student.getUpdatedAt()).build());

            System.out.println("Prepared DynamoDB item: " + item);

            PutItemRequest request = PutItemRequest.builder()
                .tableName(studentsTable)
                .item(item)
                .build();

            System.out.println("Sending PutItemRequest to DynamoDB");
            PutItemResponse response = client.putItem(request);
            System.out.println("DynamoDB response: " + response);
            
            return student;
        } catch (Exception e) {
            System.err.println("Error creating student in DynamoDB: " + e.getMessage());
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Stack trace:");
            e.printStackTrace();
            return null;
        }
    }

    public Student getStudent(String id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(studentsTable)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build();

            GetItemResponse response = client.getItem(request);
            if (!response.hasItem()) {
                return null;
            }

            Map<String, AttributeValue> item = response.item();
            Student student = new Student();
            student.setId(item.get("id").s());
            student.setName(item.get("name").s());
            student.setEmail(item.get("email").s());
            student.setMajor(item.get("major").s());
            student.setEnrollmentYear(Integer.parseInt(item.get("enrollmentYear").n()));
            student.setGraduationYear(Integer.parseInt(item.get("graduationYear").n()));
            student.setCreatedAt(item.get("createdAt").s());
            student.setUpdatedAt(item.get("updatedAt").s());

            return student;
        } catch (Exception e) {
            System.err.println("Error getting student: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Student> listStudents() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(studentsTable)
                .build();

            ScanResponse response = client.scan(request);
            List<Student> students = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                Student student = new Student();
                student.setId(item.get("id").s());
                student.setName(item.get("name").s());
                student.setEmail(item.get("email").s());
                student.setMajor(item.get("major").s());
                student.setEnrollmentYear(Integer.parseInt(item.get("enrollmentYear").n()));
                student.setGraduationYear(Integer.parseInt(item.get("graduationYear").n()));
                student.setCreatedAt(item.get("createdAt").s());
                student.setUpdatedAt(item.get("updatedAt").s());
                students.add(student);
            }

            return students;
        } catch (Exception e) {
            System.err.println("Error listing students: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Course Methods
    public Course createCourse(Course course) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(course.getId()).build());
            item.put("code", AttributeValue.builder().s(course.getCode()).build());
            item.put("name", AttributeValue.builder().s(course.getName()).build());
            item.put("description", AttributeValue.builder().s(course.getDescription()).build());
            item.put("credits", AttributeValue.builder().n(String.valueOf(course.getCredits())).build());
            item.put("instructor", AttributeValue.builder().s(course.getInstructor()).build());
            item.put("semester", AttributeValue.builder().s(course.getSemester()).build());
            item.put("createdAt", AttributeValue.builder().s(course.getCreatedAt()).build());
            item.put("updatedAt", AttributeValue.builder().s(course.getUpdatedAt()).build());

            PutItemRequest request = PutItemRequest.builder()
                .tableName(coursesTable)
                .item(item)
                .build();

            client.putItem(request);
            return course;
        } catch (Exception e) {
            System.err.println("Error creating course: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Course getCourse(String id) {
        try {
            GetItemRequest request = GetItemRequest.builder()
                .tableName(coursesTable)
                .key(Map.of("id", AttributeValue.builder().s(id).build()))
                .build();

            GetItemResponse response = client.getItem(request);
            if (!response.hasItem()) {
                return null;
            }

            Map<String, AttributeValue> item = response.item();
            Course course = new Course();
            course.setId(item.get("id").s());
            course.setCode(item.get("code").s());
            course.setName(item.get("name").s());
            course.setDescription(item.get("description").s());
            course.setCredits(Integer.parseInt(item.get("credits").n()));
            course.setInstructor(item.get("instructor").s());
            course.setSemester(item.get("semester").s());
            course.setCreatedAt(item.get("createdAt").s());
            course.setUpdatedAt(item.get("updatedAt").s());

            return course;
        } catch (Exception e) {
            System.err.println("Error getting course: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Course> listCourses() {
        try {
            ScanRequest request = ScanRequest.builder()
                .tableName(coursesTable)
                .build();

            ScanResponse response = client.scan(request);
            List<Course> courses = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                Course course = new Course();
                course.setId(item.get("id").s());
                course.setCode(item.get("code").s());
                course.setName(item.get("name").s());
                course.setDescription(item.get("description").s());
                course.setCredits(Integer.parseInt(item.get("credits").n()));
                course.setInstructor(item.get("instructor").s());
                course.setSemester(item.get("semester").s());
                course.setCreatedAt(item.get("createdAt").s());
                course.setUpdatedAt(item.get("updatedAt").s());
                courses.add(course);
            }

            return courses;
        } catch (Exception e) {
            System.err.println("Error listing courses: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Grade Methods
    public Grade createGrade(Grade grade) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(grade.getId()).build());
            item.put("studentId", AttributeValue.builder().s(grade.getStudentId()).build());
            item.put("courseId", AttributeValue.builder().s(grade.getCourseId()).build());
            item.put("grade", AttributeValue.builder().s(grade.getGrade()).build());
            item.put("semester", AttributeValue.builder().s(grade.getSemester()).build());
            item.put("createdAt", AttributeValue.builder().s(grade.getCreatedAt()).build());
            item.put("updatedAt", AttributeValue.builder().s(grade.getUpdatedAt()).build());

            PutItemRequest request = PutItemRequest.builder()
                .tableName(gradesTable)
                .item(item)
                .build();

            client.putItem(request);
            return grade;
        } catch (Exception e) {
            System.err.println("Error creating grade: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<Grade> getStudentGrades(String studentId) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#studentId", "studentId");

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":studentId", AttributeValue.builder().s(studentId).build());

            QueryRequest request = QueryRequest.builder()
                .tableName(gradesTable)
                .indexName("studentId-index")
                .keyConditionExpression("#studentId = :studentId")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

            QueryResponse response = client.query(request);
            List<Grade> grades = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                Grade grade = new Grade();
                grade.setId(item.get("id").s());
                grade.setStudentId(item.get("studentId").s());
                grade.setCourseId(item.get("courseId").s());
                grade.setGrade(item.get("grade").s());
                grade.setSemester(item.get("semester").s());
                grade.setCreatedAt(item.get("createdAt").s());
                grade.setUpdatedAt(item.get("updatedAt").s());
                grades.add(grade);
            }

            return grades;
        } catch (Exception e) {
            System.err.println("Error getting student grades: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Grade> getCourseGrades(String courseId) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#courseId", "courseId");

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":courseId", AttributeValue.builder().s(courseId).build());

            QueryRequest request = QueryRequest.builder()
                .tableName(gradesTable)
                .indexName("courseId-index")
                .keyConditionExpression("#courseId = :courseId")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

            QueryResponse response = client.query(request);
            List<Grade> grades = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                Grade grade = new Grade();
                grade.setId(item.get("id").s());
                grade.setStudentId(item.get("studentId").s());
                grade.setCourseId(item.get("courseId").s());
                grade.setGrade(item.get("grade").s());
                grade.setSemester(item.get("semester").s());
                grade.setCreatedAt(item.get("createdAt").s());
                grade.setUpdatedAt(item.get("updatedAt").s());
                grades.add(grade);
            }

            return grades;
        } catch (Exception e) {
            System.err.println("Error getting course grades: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public CourseRegistration createRegistration(CourseRegistration registration) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(registration.getId()).build());
            item.put("studentId", AttributeValue.builder().s(registration.getStudentId()).build());
            item.put("courseId", AttributeValue.builder().s(registration.getCourseId()).build());
            item.put("status", AttributeValue.builder().s(registration.getStatus()).build());
            item.put("createdAt", AttributeValue.builder().s(registration.getCreatedAt()).build());
            item.put("updatedAt", AttributeValue.builder().s(registration.getUpdatedAt()).build());

            PutItemRequest request = PutItemRequest.builder()
                .tableName(registrationsTable)
                .item(item)
                .build();

            client.putItem(request);
            return registration;
        } catch (Exception e) {
            System.err.println("Error creating registration: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public CourseRegistration updateRegistrationStatus(String registrationId, String status) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("id", AttributeValue.builder().s(registrationId).build());

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":status", AttributeValue.builder().s(status).build());
            expressionAttributeValues.put(":updatedAt", AttributeValue.builder().s(Instant.now().toString()).build());

            UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(registrationsTable)
                .key(key)
                .updateExpression("SET #status = :status, updatedAt = :updatedAt")
                .expressionAttributeNames(Map.of("#status", "status"))
                .expressionAttributeValues(expressionAttributeValues)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

            UpdateItemResponse response = client.updateItem(request);
            
            Map<String, AttributeValue> attributes = response.attributes();
            CourseRegistration updated = new CourseRegistration();
            updated.setId(attributes.get("id").s());
            updated.setStudentId(attributes.get("studentId").s());
            updated.setCourseId(attributes.get("courseId").s());
            updated.setStatus(attributes.get("status").s());
            updated.setCreatedAt(attributes.get("createdAt").s());
            updated.setUpdatedAt(attributes.get("updatedAt").s());
            
            return updated;
        } catch (Exception e) {
            System.err.println("Error updating registration: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public List<CourseRegistration> getStudentRegistrations(String studentId) {
        try {
            QueryRequest request = QueryRequest.builder()
                .tableName(registrationsTable)
                .indexName("StudentIdIndex")
                .keyConditionExpression("#studentId = :studentId")
                .expressionAttributeNames(Map.of("#studentId", "studentId"))
                .expressionAttributeValues(Map.of(":studentId", AttributeValue.builder().s(studentId).build()))
                .build();

            QueryResponse response = client.query(request);
            List<CourseRegistration> registrations = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                CourseRegistration registration = new CourseRegistration();
                registration.setId(item.get("id").s());
                registration.setStudentId(item.get("studentId").s());
                registration.setCourseId(item.get("courseId").s());
                registration.setStatus(item.get("status").s());
                registration.setCreatedAt(item.get("createdAt").s());
                registration.setUpdatedAt(item.get("updatedAt").s());
                registrations.add(registration);
            }

            return registrations;
        } catch (Exception e) {
            System.err.println("Error getting student registrations: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<CourseRegistration> getTeacherPendingRegistrations(String teacherId) {
        try {
            // First get all courses for this teacher
            QueryRequest coursesRequest = QueryRequest.builder()
                .tableName(coursesTable)
                .indexName("InstructorIndex")
                .keyConditionExpression("#instructor = :teacherId")
                .expressionAttributeNames(Map.of("#instructor", "instructor"))
                .expressionAttributeValues(Map.of(":teacherId", AttributeValue.builder().s(teacherId).build()))
                .build();

            List<String> courseIds = client.query(coursesRequest).items().stream()
                .map(item -> item.get("id").s())
                .collect(Collectors.toList());

            // Then get all pending registrations for these courses
            List<CourseRegistration> allRegistrations = new ArrayList<>();
            for (String courseId : courseIds) {
                QueryRequest registrationsRequest = QueryRequest.builder()
                    .tableName(registrationsTable)
                    .indexName("CourseIdIndex")
                    .keyConditionExpression("#courseId = :courseId")
                    .filterExpression("#status = :status")
                    .expressionAttributeNames(Map.of(
                        "#courseId", "courseId",
                        "#status", "status"
                    ))
                    .expressionAttributeValues(Map.of(
                        ":courseId", AttributeValue.builder().s(courseId).build(),
                        ":status", AttributeValue.builder().s("pending").build()
                    ))
                    .build();

                List<CourseRegistration> courseRegistrations = client.query(registrationsRequest).items().stream()
                    .map(item -> {
                        CourseRegistration registration = new CourseRegistration();
                        registration.setId(item.get("id").s());
                        registration.setStudentId(item.get("studentId").s());
                        registration.setCourseId(item.get("courseId").s());
                        registration.setStatus(item.get("status").s());
                        registration.setCreatedAt(item.get("createdAt").s());
                        registration.setUpdatedAt(item.get("updatedAt").s());
                        return registration;
                    })
                    .collect(Collectors.toList());

                allRegistrations.addAll(courseRegistrations);
            }

            return allRegistrations;
        } catch (Exception e) {
            System.err.println("Error getting teacher pending registrations: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<String> getCourseIdsByInstructor(String instructorId) {
        try {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put("#instructor", "instructor");

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":instructor", AttributeValue.builder().s(instructorId).build());

            QueryRequest request = QueryRequest.builder()
                .tableName(coursesTable)
                .indexName("InstructorIndex")
                .keyConditionExpression("#instructor = :instructor")
                .expressionAttributeNames(expressionAttributeNames)
                .expressionAttributeValues(expressionAttributeValues)
                .build();

            QueryResponse response = client.query(request);
            return response.items().stream()
                .map(item -> item.get("id").s())
                .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<CourseRegistration> getPendingRegistrationsByCourseIds(List<String> courseIds) {
        try {
            List<CourseRegistration> registrations = new ArrayList<>();
            
            for (String courseId : courseIds) {
                Map<String, String> expressionAttributeNames = new HashMap<>();
                expressionAttributeNames.put("#courseId", "courseId");
                expressionAttributeNames.put("#status", "status");

                Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                expressionAttributeValues.put(":courseId", AttributeValue.builder().s(courseId).build());
                expressionAttributeValues.put(":status", AttributeValue.builder().s("PENDING").build());

                QueryRequest request = QueryRequest.builder()
                    .tableName(registrationsTable)
                    .indexName("CourseIdIndex")
                    .keyConditionExpression("#courseId = :courseId")
                    .filterExpression("#status = :status")
                    .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .build();

                QueryResponse response = client.query(request);
                
                for (Map<String, AttributeValue> item : response.items()) {
                    CourseRegistration registration = new CourseRegistration();
                    registration.setId(item.get("id").s());
                    registration.setStudentId(item.get("studentId").s());
                    registration.setCourseId(item.get("courseId").s());
                    registration.setStatus(item.get("status").s());
                    registration.setCreatedAt(item.get("createdAt").s());
                    registration.setUpdatedAt(item.get("updatedAt").s());
                    registrations.add(registration);
                }
            }
            
            return registrations;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public Course updateCourse(Course course) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.builder().s(course.getId()).build());
            item.put("code", AttributeValue.builder().s(course.getCode()).build());
            item.put("name", AttributeValue.builder().s(course.getName()).build());
            item.put("description", AttributeValue.builder().s(course.getDescription()).build());
            item.put("credits", AttributeValue.builder().n(String.valueOf(course.getCredits())).build());
            item.put("instructor", AttributeValue.builder().s(course.getInstructor()).build());
            item.put("semester", AttributeValue.builder().s(course.getSemester()).build());
            item.put("updatedAt", AttributeValue.builder().s(course.getUpdatedAt()).build());
    
            PutItemRequest request = PutItemRequest.builder()
                .tableName(coursesTable)
                .item(item)
                .build();
    
            client.putItem(request);
            return course;
        } catch (Exception e) {
            System.err.println("Error updating course: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
} 