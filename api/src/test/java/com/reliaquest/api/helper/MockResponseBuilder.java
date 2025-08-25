package com.reliaquest.api.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.Employee;
import java.util.List;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import org.springframework.http.MediaType;

public class MockResponseBuilder {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SUCCESS_STATUS = "Successfully processed request.";

    public static MockResponse success(Object data) {
        return success(data, 200);
    }

    public static MockResponse created(Object data) {
        return success(data, 201);
    }

    public static MockResponse success(Object data, int statusCode) {
        try {
            Map<String, Object> response = Map.of(
                    "data", data,
                    "status", SUCCESS_STATUS);
            String json = objectMapper.writeValueAsString(response);
            return new MockResponse()
                    .setResponseCode(statusCode)
                    .setBody(json)
                    .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize mock response", e);
        }
    }

    public static MockResponse notFound() {
        return new MockResponse().setResponseCode(404);
    }

    public static MockResponse serverError() {
        return new MockResponse().setResponseCode(500);
    }

    public static MockResponse rateLimited() {
        return new MockResponse().setResponseCode(429);
    }

    public static MockResponse timeout() {
        // Disconnect during request to simulate network failure
        return new MockResponse().setSocketPolicy(okhttp3.mockwebserver.SocketPolicy.DISCONNECT_DURING_REQUEST_BODY);
    }

    public static MockResponse malformed() {
        return new MockResponse()
                .setBody("{ invalid json }")
                .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
    }

    public static MockResponse emptyList() {
        return success(List.of());
    }

    public static class EmployeeBuilder {
        public static Employee create(String id, String name, int salary, int age, String title) {
            return Employee.builder()
                    .id(id)
                    .employeeName(name)
                    .employeeSalary(salary)
                    .employeeAge(age)
                    .employeeTitle(title)
                    .employeeEmail(generateEmail(name))
                    .build();
        }

        public static Employee tonyFadel() {
            return create("00cf6be1-eb26-428d-8ac7-7b7e337b3526", "Tony Fadel", 495896, 22, "Mining Supervisor");
        }

        public static Employee elvieBernhard() {
            return create(
                    "5231a497-e5cd-493c-ae18-9e27dd3d5684", "Elvie Bernhard", 492147, 25, "Manufacturing Coordinator");
        }

        public static Employee jeanCarroll() {
            return create("59564fc6-2f98-48ba-b240-a1921a29cb48", "Jean Carroll", 484716, 55, "Corporate IT Designer");
        }

        public static Employee eugeneGraham() {
            return create(
                    "8e00c3e4-2526-4ffd-825f-def4cef223ab", "Eugene Graham", 466985, 68, "Forward Sales Specialist");
        }

        public static Employee aracelisKiehn() {
            return create(
                    "5b39b2d6-fa65-4dca-8ec4-b82458415419", "Aracelis Kiehn II", 463390, 22, "National Facilitator");
        }

        public static Employee jarodSauer() {
            return create("75c6110d-f44f-4dcf-a185-82f3585ba635", "Jarod Sauer", 459440, 20, "Customer Assistant");
        }

        public static Employee charlesettaDouglas() {
            return create(
                    "6e3e604c-f55e-441b-a38d-adc3dda2678b",
                    "Charlesetta Douglas",
                    458892,
                    33,
                    "Regional Retail Analyst");
        }

        public static Employee corrineWard() {
            return create(
                    "091456c8-d6b1-47ba-8c1a-23adf51445d3", "Corrine Ward", 457144, 39, "Principal Sales Strategist");
        }

        public static Employee carrollBartoletti() {
            return create(
                    "862ab45d-0978-4f29-acd5-bd0be63d86f9",
                    "Carroll Bartoletti",
                    448438,
                    27,
                    "Chief Consulting Manager");
        }

        public static Employee nevaOReilly() {
            return create(
                    "19cdced5-0242-4584-b8a3-2e474029f2ef", "Neva O'Reilly", 445359, 62, "District Retail Engineer");
        }

        public static Employee arturoMuller() {
            return create("4aa6b121-6f33-4ca6-840b-147635d7ea2f", "Arturo Muller", 373162, 66, "Retail Facilitator");
        }

        public static Employee arturoMarks() {
            return create("8ab1bd30-36cb-4e22-b6e3-64aa31076436", "Arturo Marks", 375674, 48, "Hospitality Developer");
        }

        public static Employee williamGlover() {
            return create(
                    "55d49602-b5d7-4d13-80e2-408b85e6fc58", "William Glover", 267329, 26, "Internal Representative");
        }

        public static Employee rosarioGoldner() {
            return create(
                    "a0e16ca6-30b8-4226-afc5-c2ca851e5ad3", "Rosario Goldner II", 137452, 35, "National Associate");
        }

        public static Employee ngocHand() {
            return create(
                    "203ae88a-a0ae-4ad6-b321-5c5ba0be5afc", "Ngoc Hand", 93054, 21, "Internal Accounting Director");
        }

        public static List<Employee> topTenByHighestSalary() {
            return List.of(
                    tonyFadel(),
                    elvieBernhard(),
                    jeanCarroll(),
                    eugeneGraham(),
                    aracelisKiehn(),
                    jarodSauer(),
                    charlesettaDouglas(),
                    corrineWard(),
                    carrollBartoletti(),
                    nevaOReilly());
        }

        public static List<Employee> allWithArturosInName() {
            return List.of(arturoMuller(), arturoMarks());
        }

        private static String generateEmail(String name) {
            return name.toLowerCase().replaceAll(" ", ".") + "@company.com";
        }
    }
}
