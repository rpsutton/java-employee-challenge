package com.reliaquest.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmployeeValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidEmployee() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).isEmpty();
    }

    @Test
    void testEmployeeWithBlankName() {
        Employee employee = Employee.builder()
                .employeeName("")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name cannot be blank");
    }

    @Test
    void testEmployeeWithNullName() {
        Employee employee = Employee.builder()
                .employeeName(null)
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name cannot be blank");
    }

    @Test
    void testEmployeeWithNegativeSalary() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(-1000)
                .employeeAge(30)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Salary must be greater than zero");
    }

    @Test
    void testEmployeeWithZeroSalary() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(0)
                .employeeAge(30)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Salary must be greater than zero");
    }

    @Test
    void testEmployeeWithNullSalary() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(null)
                .employeeAge(30)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Salary is required");
    }

    @Test
    void testEmployeeWithAgeTooYoung() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(15)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Age must be at least 16");
    }

    @Test
    void testEmployeeWithAgeTooOld() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(76)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Age cannot exceed 75");
    }

    @Test
    void testEmployeeWithValidBoundaryAges() {
        Employee youngEmployee = Employee.builder()
                .employeeName("Young Employee")
                .employeeSalary(30000)
                .employeeAge(16)
                .employeeTitle("Intern")
                .build();

        Set<ConstraintViolation<Employee>> youngViolations = validator.validate(youngEmployee, CreateValidation.class);
        assertThat(youngViolations).isEmpty();

        Employee oldEmployee = Employee.builder()
                .employeeName("Senior Employee")
                .employeeSalary(100000)
                .employeeAge(75)
                .employeeTitle("Senior Consultant")
                .build();

        Set<ConstraintViolation<Employee>> oldViolations = validator.validate(oldEmployee, CreateValidation.class);
        assertThat(oldViolations).isEmpty();
    }

    @Test
    void testEmployeeWithNullAge() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(null)
                .employeeTitle("Software Engineer")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Age is required");
    }

    @Test
    void testEmployeeWithBlankTitle() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle("")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title cannot be blank");
    }

    @Test
    void testEmployeeWithNullTitle() {
        Employee employee = Employee.builder()
                .employeeName("John Doe")
                .employeeSalary(50000)
                .employeeAge(30)
                .employeeTitle(null)
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title cannot be blank");
    }

    @Test
    void testEmployeeWithMultipleViolations() {
        Employee employee = Employee.builder()
                .employeeName("")
                .employeeSalary(-1000)
                .employeeAge(10)
                .employeeTitle("")
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee, CreateValidation.class);
        assertThat(violations).hasSize(4);
    }

    @Test
    void testEmployeeWithoutValidationGroup() {
        Employee employee = Employee.builder()
                .employeeName(null)
                .employeeSalary(null)
                .employeeAge(null)
                .employeeTitle(null)
                .build();

        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);
        assertThat(violations).isEmpty();
    }
}