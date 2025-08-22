package com.reliaquest.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.reliaquest.api.config.MockApiProperties;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private WebClient mockWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private MockApiProperties mockApiProperties;

    @Mock
    private MockApiProperties.RetryConfig retryConfig;

    private EmployeeService employeeService;

    private Employee testEmployee1;
    private Employee testEmployee2;
    private List<Employee> testEmployees;

    @BeforeEach
    void setUp() {
        when(mockApiProperties.getRetry()).thenReturn(retryConfig);
        when(retryConfig.getMaxAttempts()).thenReturn(3);
        when(retryConfig.getInitialDelay()).thenReturn(10L);
        when(retryConfig.getMaxDelay()).thenReturn(50L);

        employeeService = new EmployeeService(mockWebClient, mockApiProperties);

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
    void getAllEmployees_shouldReturnListOfEmployees() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.getAllEmployees())
                .assertNext(employees -> {
                    assertThat(employees).hasSize(2);
                    assertThat(employees).containsExactlyInAnyOrder(testEmployee1, testEmployee2);
                })
                .verifyComplete();
    }

    @Test
    void getAllEmployees_shouldHandleSuccessAfterRetry() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.getAllEmployees())
                .assertNext(employees -> {
                    assertThat(employees).hasSize(2);
                    assertThat(employees).containsExactlyInAnyOrder(testEmployee1, testEmployee2);
                })
                .verifyComplete();
    }

    @Test
    void getAllEmployees_shouldThrowAfterMaxRetries() {
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

        WebClientResponseException rateLimitError =
                WebClientResponseException.create(429, "Too Many Requests", null, null, null);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.error(rateLimitError));

        StepVerifier.create(employeeService.getAllEmployees())
                .expectErrorMatches(throwable -> {
                    return throwable instanceof RuntimeException
                            && throwable.getMessage() != null
                            && throwable.getMessage().contains("Service unavailable after 3 retry attempts");
                })
                .verify();
    }

    @Test
    void searchEmployeesByName_shouldFilterEmployees() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.searchEmployeesByName("John"))
                .assertNext(employees -> {
                    assertThat(employees).hasSize(1);
                    assertThat(employees.get(0).getEmployeeName()).isEqualTo("John Doe");
                })
                .verifyComplete();
    }

    @Test
    void searchEmployeesByName_shouldBeCaseInsensitive() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.searchEmployeesByName("SMITH"))
                .assertNext(employees -> {
                    assertThat(employees).hasSize(1);
                    assertThat(employees.get(0).getEmployeeName()).isEqualTo("Jane Smith");
                })
                .verifyComplete();
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        ApiResponse<Employee> apiResponse = new ApiResponse<>(testEmployee1, "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{id}", "123")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.getEmployeeById("123"))
                .assertNext(employee -> {
                    assertThat(employee).isNotNull();
                    assertThat(employee.getId()).isEqualTo("123");
                    assertThat(employee.getEmployeeName()).isEqualTo("John Doe");
                })
                .verifyComplete();
    }

    @Test
    void getEmployeeById_shouldReturnEmptyWhenNotFound() {
        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{id}", "999")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(WebClientResponseException.create(404, "Not Found", null, null, null)));

        StepVerifier.create(employeeService.getEmployeeById("999")).verifyComplete();
    }

    @Test
    void getHighestSalary_shouldReturnMaxSalary() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(testEmployees, "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.getHighestSalary())
                .assertNext(salary -> {
                    assertThat(salary).isEqualTo(120000);
                })
                .verifyComplete();
    }

    @Test
    void getHighestSalary_shouldReturnZeroWhenNoEmployees() {
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(List.of(), "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.getHighestSalary())
                .assertNext(salary -> {
                    assertThat(salary).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    void getTop10HighestEarningEmployeeNames_shouldReturnSortedNames() {
        List<Employee> manyEmployees = Arrays.asList(
                Employee.builder()
                        .employeeName("Low Earner")
                        .employeeSalary(50000)
                        .build(),
                Employee.builder()
                        .employeeName("High Earner")
                        .employeeSalary(200000)
                        .build(),
                Employee.builder()
                        .employeeName("Mid Earner")
                        .employeeSalary(100000)
                        .build());
        ApiResponse<List<Employee>> apiResponse = new ApiResponse<>(manyEmployees, "Success");

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.getTop10HighestEarningEmployeeNames())
                .assertNext(names -> {
                    assertThat(names).hasSize(3);
                    assertThat(names.get(0)).isEqualTo("High Earner");
                    assertThat(names.get(1)).isEqualTo("Mid Earner");
                    assertThat(names.get(2)).isEqualTo("Low Earner");
                })
                .verifyComplete();
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
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

        ApiResponse<Employee> apiResponse = new ApiResponse<>(createdEmployee, "Success");

        when(mockWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(request)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(apiResponse));

        StepVerifier.create(employeeService.createEmployee(request))
                .assertNext(employee -> {
                    assertThat(employee).isNotNull();
                    assertThat(employee.getId()).isEqualTo("789");
                    assertThat(employee.getEmployeeName()).isEqualTo("New Employee");
                })
                .verifyComplete();
    }

    @Test
    void deleteEmployeeById_shouldReturnEmployeeName() {
        ApiResponse<Employee> getResponse = new ApiResponse<>(testEmployee1, "Success");
        ApiResponse<Boolean> deleteResponse = new ApiResponse<>(true, "Success");

        WebClient.ResponseSpec getResponseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);
        WebClient.ResponseSpec deleteResponseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{id}", "123")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(getResponseSpec);
        when(getResponseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(getResponse));

        WebClient.RequestBodyUriSpec deleteRequestBodyUriSpec =
                org.mockito.Mockito.mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec deleteRequestBodySpec = org.mockito.Mockito.mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec deleteRequestHeadersSpec =
                org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);

        when(mockWebClient.method(HttpMethod.DELETE)).thenReturn(deleteRequestBodyUriSpec);
        when(deleteRequestBodyUriSpec.uri("/{name}", "John Doe")).thenReturn(deleteRequestBodySpec);
        when(deleteRequestBodySpec.bodyValue(any(Map.class))).thenReturn(deleteRequestHeadersSpec);
        when(deleteRequestHeadersSpec.retrieve()).thenReturn(deleteResponseSpec);
        when(deleteResponseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(deleteResponse));

        StepVerifier.create(employeeService.deleteEmployeeById("123"))
                .assertNext(name -> {
                    assertThat(name).isEqualTo("John Doe");
                })
                .verifyComplete();
    }

    @Test
    void deleteEmployeeById_shouldThrowWhenEmployeeNotFound() {
        WebClient.ResponseSpec notFoundResponseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);

        when(mockWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{id}", "999")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(notFoundResponseSpec);
        when(notFoundResponseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(WebClientResponseException.create(404, "Not Found", null, null, null)));

        StepVerifier.create(employeeService.deleteEmployeeById("999"))
                .expectErrorMessage("Employee not found with id: 999")
                .verify();
    }
}
