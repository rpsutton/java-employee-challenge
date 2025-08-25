package com.reliaquest.api.service;

import com.reliaquest.api.config.MockApiProperties;
import com.reliaquest.api.model.ApiResponse;
import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final WebClient mockApiWebClient;
    private final MockApiProperties mockApiProperties;

    private Retry getRetrySpec() {
        MockApiProperties.RetryConfig retryConfig = mockApiProperties.getRetry();

        return Retry.backoff(retryConfig.getMaxAttempts(), Duration.ofMillis(retryConfig.getInitialDelay()))
                .maxBackoff(Duration.ofMillis(retryConfig.getMaxDelay()))
                .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
                .doBeforeRetry(retrySignal -> log.warn(
                        "Rate limited, retry attempt {} of {}",
                        retrySignal.totalRetries() + 1,
                        retryConfig.getMaxAttempts()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new RuntimeException(
                        "Service unavailable after " + retryConfig.getMaxAttempts() + " retry attempts",
                        retrySignal.failure()));
    }

    public Mono<List<Employee>> getAllEmployees() {
        log.info("Fetching all employees from mock API");

        return mockApiWebClient
                .get()
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Employee>>>() {})
                .retryWhen(getRetrySpec())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        log.info(
                                "Successfully fetched {} employees",
                                response.getData().size());
                        return response.getData();
                    }
                    List<Employee> emptyList = List.of();
                    return emptyList;
                })
                .doOnError(error -> log.error("Error fetching all employees", error));
    }

    public Mono<List<Employee>> searchEmployeesByName(String searchString) {
        log.info("Searching employees by name: {}", searchString);

        return getAllEmployees().map(employees -> {
            String lowerSearchString = searchString.toLowerCase();
            List<Employee> filtered = employees.stream()
                    .filter(emp -> emp.getEmployeeName() != null
                            && emp.getEmployeeName().toLowerCase().contains(lowerSearchString))
                    .toList();

            log.info("Found {} employees matching search criteria", filtered.size());
            return filtered;
        });
    }

    public Mono<Employee> getEmployeeById(String id) {
        log.info("Fetching employee by id: {}", id);

        return mockApiWebClient
                .get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Employee>>() {})
                .retryWhen(getRetrySpec())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        log.info("Successfully fetched employee with id: {}", id);
                        return response.getData();
                    }
                    return null;
                })
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("Employee not found with id: {}", id);
                    return Mono.empty();
                })
                .doOnError(error -> log.error("Error fetching employee by id: {}", id, error));
    }

    public Mono<Integer> getHighestSalary() {
        log.info("Finding highest salary among all employees");

        return getAllEmployees().map(employees -> {
            Integer highestSalary = employees.stream()
                    .map(Employee::getEmployeeSalary)
                    .filter(salary -> salary != null)
                    .max(Integer::compareTo)
                    .orElse(0);

            log.info("Highest salary found: {}", highestSalary);
            return highestSalary;
        });
    }

    public Mono<List<String>> getTop10HighestEarningEmployeeNames() {
        log.info("Finding top 10 highest earning employees");

        return getAllEmployees().map(employees -> {
            List<String> topEarners = employees.stream()
                    .filter(emp -> emp.getEmployeeSalary() != null)
                    .sorted(Comparator.comparing(Employee::getEmployeeSalary).reversed())
                    .limit(10)
                    .map(Employee::getEmployeeName)
                    .toList();

            log.info("Found {} top earners", topEarners.size());
            return topEarners;
        });
    }

    public Mono<Employee> createEmployee(CreateEmployeeRequest request) {
        log.info("Creating new employee: {}", request.getName());

        return mockApiWebClient
                .post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<Employee>>() {})
                .retryWhen(getRetrySpec())
                .map(response -> {
                    if (response != null && response.getData() != null) {
                        log.info(
                                "Successfully created employee with id: {}",
                                response.getData().getId());
                        return response.getData();
                    }
                    return null;
                })
                .doOnError(error -> log.error("Error creating employee: {}", request.getName(), error));
    }

    public Mono<String> deleteEmployeeById(String id) {
        log.info("Attempting to delete employee by id: {}", id);

        return getEmployeeById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Employee not found with id: " + id)))
                .flatMap(employee -> {
                    String employeeName = employee.getEmployeeName();
                    log.info("Found employee '{}' with id '{}', proceeding with deletion", employeeName, id);

                    Map<String, String> requestBody = Map.of("name", employeeName);

                    return mockApiWebClient
                            .method(org.springframework.http.HttpMethod.DELETE)
                            .uri("")
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<ApiResponse<Boolean>>() {})
                            .retryWhen(getRetrySpec())
                            .map(response -> {
                                if (response != null && Boolean.TRUE.equals(response.getData())) {
                                    log.info("Successfully deleted employee '{}' with id '{}'", employeeName, id);
                                    return employeeName;
                                } else {
                                    throw new RuntimeException("Failed to delete employee");
                                }
                            });
                })
                .doOnError(error -> log.error("Error deleting employee with id: {}", id, error));
    }
}
