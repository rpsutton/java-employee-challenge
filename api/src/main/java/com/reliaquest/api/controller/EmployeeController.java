package com.reliaquest.api.controller;

import com.reliaquest.api.model.CreateEmployeeRequest;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.service.EmployeeService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeRequest> {

    private final EmployeeService employeeService;
    private final Validator validator;

    @Override
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.debug("GET request to fetch all employees");
        return employeeService
                .getAllEmployees()
                .map(ResponseEntity::ok)
                .doOnError(error -> log.error("Error in getAllEmployees endpoint", error))
                .onErrorReturn(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .block();
    }

    @Override
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        log.debug("GET request to search employees by name: {}", searchString);
        return employeeService
                .searchEmployeesByName(searchString)
                .map(ResponseEntity::ok)
                .doOnError(error -> log.error("Error in getEmployeesByNameSearch endpoint", error))
                .onErrorReturn(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .block();
    }

    @Override
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) {
        log.debug("GET request to fetch employee by id: {}", id);
        return employeeService
                .getEmployeeById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnError(error -> log.error("Error in getEmployeeById endpoint", error))
                .onErrorReturn(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .block();
    }

    @Override
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.debug("GET request to fetch highest salary");
        return employeeService
                .getHighestSalary()
                .map(ResponseEntity::ok)
                .doOnError(error -> log.error("Error in getHighestSalaryOfEmployees endpoint", error))
                .onErrorReturn(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .block();
    }

    @Override
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.debug("GET request to fetch top 10 highest earning employees");
        return employeeService
                .getTop10HighestEarningEmployeeNames()
                .map(ResponseEntity::ok)
                .doOnError(error -> log.error("Error in getTopTenHighestEarningEmployeeNames endpoint", error))
                .onErrorReturn(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .block();
    }

    @Override
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeRequest employeeInput) {
        log.debug("POST request to create employee: {}", employeeInput.getName());

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(employeeInput);
        if (!violations.isEmpty()) {
            log.warn("Validation failed for create employee request: {}", violations);
            return ResponseEntity.badRequest().build();
        }

        return employeeService
                .createEmployee(employeeInput)
                .map(employee -> ResponseEntity.status(HttpStatus.CREATED).body(employee))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())
                .doOnError(error -> log.error("Error in createEmployee endpoint", error))
                .onErrorReturn(
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build())
                .block();
    }

    @Override
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.debug("DELETE request for employee id: {}", id);
        try {
            String deletedEmployeeName = employeeService
                    .deleteEmployeeById(id)
                    .doOnError(error -> log.error("Error in deleteEmployeeById endpoint", error))
                    .block();
            return ResponseEntity.ok(deletedEmployeeName);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            log.error("Error deleting employee", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
