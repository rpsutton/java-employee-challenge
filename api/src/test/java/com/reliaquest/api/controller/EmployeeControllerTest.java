package com.reliaquest.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;
    
    @MockBean
    private Validator validator;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        testEmployee1 = Employee.builder()
                .id("123")
                .employeeName("John Doe")
                .employeeSalary(100000)
                .employeeAge(30)
                .employeeTitle("Software Engineer")
                .employeeEmail("john@company.com")
                .build();

        testEmployee2 = Employee.builder()
                .id("456")
                .employeeName("Jane Smith")
                .employeeSalary(120000)
                .employeeAge(35)
                .employeeTitle("Senior Engineer")
                .employeeEmail("jane@company.com")
                .build();

        testEmployees = Arrays.asList(testEmployee1, testEmployee2);
    }

    @Test
    void getAllEmployees_shouldReturn200WithEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Mono.just(testEmployees));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("123"))
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value("456"))
                .andExpect(jsonPath("$[1].employee_name").value("Jane Smith"));
    }

    @Test
    void getAllEmployees_shouldReturn500OnError() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Mono.error(new RuntimeException("Service error")));

        mockMvc.perform(get("/api/v1/employee"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnFilteredEmployees() throws Exception {
        when(employeeService.searchEmployeesByName("John"))
                .thenReturn(Mono.just(Arrays.asList(testEmployee1)));

        mockMvc.perform(get("/api/v1/employee/search/John"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].employee_name").value("John Doe"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() throws Exception {
        when(employeeService.getEmployeeById("123")).thenReturn(Mono.just(testEmployee1));

        mockMvc.perform(get("/api/v1/employee/123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.employee_name").value("John Doe"));
    }

    @Test
    void getEmployeeById_shouldReturn404WhenNotFound() throws Exception {
        when(employeeService.getEmployeeById("999")).thenReturn(Mono.empty());

        mockMvc.perform(get("/api/v1/employee/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHighestSalaryOfEmployees_shouldReturnHighestSalary() throws Exception {
        when(employeeService.getHighestSalary()).thenReturn(Mono.just(120000));

        mockMvc.perform(get("/api/v1/employee/highestSalary"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("120000"));
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnNames() throws Exception {
        List<String> topEarners = Arrays.asList("Jane Smith", "John Doe");
        when(employeeService.getTop10HighestEarningEmployeeNames()).thenReturn(Mono.just(topEarners));

        mockMvc.perform(get("/api/v1/employee/topTenHighestEarningEmployeeNames"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("Jane Smith"))
                .andExpect(jsonPath("$[1]").value("John Doe"));
    }

    @Test
    void createEmployee_shouldReturn201WithCreatedEmployee() throws Exception {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("New Employee")
                .salary(80000)
                .age(28)
                .title("Developer")
                .build();

        Employee createdEmployee = Employee.builder()
                .id("789")
                .employeeName("New Employee")
                .employeeSalary(80000)
                .employeeAge(28)
                .employeeTitle("Developer")
                .employeeEmail("new@company.com")
                .build();

        when(validator.validate(any(CreateEmployeeRequest.class))).thenReturn(new HashSet<>());
        when(employeeService.createEmployee(any(CreateEmployeeRequest.class)))
                .thenReturn(Mono.just(createdEmployee));

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("789"))
                .andExpect(jsonPath("$.employee_name").value("New Employee"));
    }

    @Test
    void createEmployee_shouldReturn400ForInvalidInput() throws Exception {
        CreateEmployeeRequest invalidRequest = CreateEmployeeRequest.builder()
                .name("")
                .salary(-1000)
                .age(10)
                .title("")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = new HashSet<>();
        ConstraintViolation<CreateEmployeeRequest> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        violations.add(violation);
        when(validator.validate(any(CreateEmployeeRequest.class))).thenReturn(violations);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEmployee_shouldReturn400ForMissingFields() throws Exception {
        String invalidJson = "{}";

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = new HashSet<>();
        ConstraintViolation<CreateEmployeeRequest> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        violations.add(violation);
        when(validator.validate(any(CreateEmployeeRequest.class))).thenReturn(violations);

        mockMvc.perform(post("/api/v1/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployeeById_shouldReturnEmployeeName() throws Exception {
        when(employeeService.deleteEmployeeById("123")).thenReturn(Mono.just("John Doe"));

        mockMvc.perform(delete("/api/v1/employee/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("John Doe"));
    }

    @Test
    void deleteEmployeeById_shouldReturn404WhenNotFound() throws Exception {
        when(employeeService.deleteEmployeeById("999"))
                .thenReturn(Mono.error(new RuntimeException("Employee not found with id: 999")));

        mockMvc.perform(delete("/api/v1/employee/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteEmployeeById_shouldReturn500OnOtherErrors() throws Exception {
        when(employeeService.deleteEmployeeById("123"))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        mockMvc.perform(delete("/api/v1/employee/123"))
                .andExpect(status().isInternalServerError());
    }
}