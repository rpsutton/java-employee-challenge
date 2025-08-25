package com.reliaquest.api;

import static com.reliaquest.api.helper.MockResponseBuilder.*;
import static com.reliaquest.api.helper.MockResponseBuilder.EmployeeBuilder.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static MockWebServer mockWebServer;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        registry.add(
                "mock-api.base-url",
                () -> String.format("http://localhost:%s/api/v1/employee", mockWebServer.getPort()));
    }

    @BeforeEach
    void setUp() {}

    @AfterEach
    void tearDown() throws InterruptedException {
        try {
            while (true) {
                RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.MILLISECONDS);
                if (request == null) {
                    break;
                }
            }
        } catch (InterruptedException e) {
        }
    }

    @Test
    void testGetAllEmployees() throws Exception {
        List<Employee> employees = List.of(tonyFadel(), elvieBernhard(), jeanCarroll());
        mockWebServer.enqueue(success(employees));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(tonyFadel().getId()))
                .andExpect(jsonPath("$[0].employee_name").value(tonyFadel().getEmployeeName()))
                .andExpect(jsonPath("$[0].employee_salary").value(tonyFadel().getEmployeeSalary()))
                .andExpect(jsonPath("$[1].employee_name").value(elvieBernhard().getEmployeeName()))
                .andExpect(jsonPath("$[2].employee_name").value(jeanCarroll().getEmployeeName()));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/employee");
    }

    @Test
    void testGetEmployeeById() throws Exception {
        Employee employee = arturoMuller();
        mockWebServer.enqueue(success(employee));

        mockMvc.perform(get("/api/v1/employee/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(employee.getId()))
                .andExpect(jsonPath("$.employee_name").value(employee.getEmployeeName()))
                .andExpect(jsonPath("$.employee_salary").value(employee.getEmployeeSalary()))
                .andExpect(jsonPath("$.employee_age").value(employee.getEmployeeAge()));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/employee/" + employee.getId());
    }

    @Test
    void testGetEmployeeById_NotFound() throws Exception {
        mockWebServer.enqueue(notFound());

        mockMvc.perform(get("/api/v1/employee/non-existent-id")).andExpect(status().isNotFound());
    }

    @Test
    void testSearchEmployeesByName() throws Exception {
        List<Employee> arturos = allWithArturosInName();
        mockWebServer.enqueue(success(arturos));

        mockMvc.perform(get("/api/v1/employee/search/Arturo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].employee_name").value(arturoMuller().getEmployeeName()))
                .andExpect(jsonPath("$[1].employee_name").value(arturoMarks().getEmployeeName()));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/employee");
    }

    @Test
    void testGetHighestSalary() throws Exception {
        List<Employee> employees = List.of(tonyFadel(), elvieBernhard());
        mockWebServer.enqueue(success(employees));

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(tonyFadel().getEmployeeSalary()));
    }

    @Test
    void testGetTop10HighestEarningEmployeeNames() throws Exception {
        List<Employee> topTen = topTenByHighestSalary();
        mockWebServer.enqueue(success(topTen));

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("Tony Fadel"))
                .andExpect(jsonPath("$[1]").value("Elvie Bernhard"))
                .andExpect(jsonPath("$[2]").value("Jean Carroll"))
                .andExpect(jsonPath("$[3]").value("Eugene Graham"))
                .andExpect(jsonPath("$[4]").value("Aracelis Kiehn II"))
                .andExpect(jsonPath("$[5]").value("Jarod Sauer"))
                .andExpect(jsonPath("$[6]").value("Charlesetta Douglas"))
                .andExpect(jsonPath("$[7]").value("Corrine Ward"))
                .andExpect(jsonPath("$[8]").value("Carroll Bartoletti"))
                .andExpect(jsonPath("$[9]").value("Neva O'Reilly"));
    }

    @Test
    void testCreateEmployee() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("William Glover")
                .salary(267329)
                .age(26)
                .title("Internal Representative")
                .build();

        Employee createdEmployee = williamGlover();
        mockWebServer.enqueue(created(createdEmployee));

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdEmployee.getId()))
                .andExpect(jsonPath("$.employee_name").value(createdEmployee.getEmployeeName()))
                .andExpect(jsonPath("$.employee_salary").value(createdEmployee.getEmployeeSalary()));

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/employee");
    }

    @Test
    void testCreateEmployee_ValidationFailure() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("")
                .salary(-1000)
                .age(15)
                .title("")
                .build();

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateEmployee_AgeValidation() throws Exception {
        CreateEmployeeRequest tooYoung = CreateEmployeeRequest.builder()
                .name("Young Employee")
                .salary(50000)
                .age(15)
                .title("Intern")
                .build();

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooYoung)))
                .andExpect(status().isBadRequest());

        CreateEmployeeRequest tooOld = CreateEmployeeRequest.builder()
                .name("Old Employee")
                .salary(100000)
                .age(76)
                .title("Consultant")
                .build();

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tooOld)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteEmployeeById() throws Exception {
        Employee employee = rosarioGoldner();

        mockWebServer.enqueue(success(employee));
        mockWebServer.enqueue(success(true));

        mockMvc.perform(delete("/api/v1/employee/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(employee.getEmployeeName()));

        RecordedRequest getRequest = mockWebServer.takeRequest();
        assertThat(getRequest.getMethod()).isEqualTo("GET");
        assertThat(getRequest.getPath()).isEqualTo("/api/v1/employee/" + employee.getId());

        RecordedRequest deleteRequest = mockWebServer.takeRequest();
        assertThat(deleteRequest.getMethod()).isEqualTo("DELETE");
        assertThat(deleteRequest.getPath()).isEqualTo("/api/v1/employee");
    }

    @Test
    void testDeleteEmployeeById_NotFound() throws Exception {
        mockWebServer.enqueue(notFound());

        mockMvc.perform(delete("/api/v1/employee/non-existent-id")).andExpect(status().isNotFound());
    }

    @Test
    void testServerError_ReturnsInternalServerError() throws Exception {
        mockWebServer.enqueue(serverError());

        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isInternalServerError());
    }

    @Test
    void testRateLimiting_WithRetry() throws Exception {
        Employee employee = ngocHand();

        int initialRequestCount = mockWebServer.getRequestCount();

        mockWebServer.enqueue(rateLimited());
        mockWebServer.enqueue(rateLimited());
        mockWebServer.enqueue(success(List.of(employee)));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employee_name").value(employee.getEmployeeName()));

        int requestsMade = mockWebServer.getRequestCount() - initialRequestCount;
        assertThat(requestsMade).isEqualTo(3);
    }

    @Test
    void testEmptyEmployeeList() throws Exception {
        mockWebServer.enqueue(emptyList());

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testMalformedResponse() throws Exception {
        mockWebServer.enqueue(malformed());

        mockMvc.perform(get("/api/v1/employee")).andExpect(status().isInternalServerError());
    }
}
