package com.reliaquest.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateEmployeeRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCreateEmployeeRequest() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(50000)
                .age(30)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void testCreateEmployeeRequestWithBlankName() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("")
                .salary(50000)
                .age(30)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name cannot be blank");
    }

    @Test
    void testCreateEmployeeRequestWithNullName() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name(null)
                .salary(50000)
                .age(30)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Name cannot be blank");
    }

    @Test
    void testCreateEmployeeRequestWithNegativeSalary() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(-1000)
                .age(30)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Salary must be greater than zero");
    }

    @Test
    void testCreateEmployeeRequestWithZeroSalary() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(0)
                .age(30)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Salary must be greater than zero");
    }

    @Test
    void testCreateEmployeeRequestWithNullSalary() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(null)
                .age(30)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Salary is required");
    }

    @Test
    void testCreateEmployeeRequestWithAgeTooYoung() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(50000)
                .age(15)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Age must be at least 16");
    }

    @Test
    void testCreateEmployeeRequestWithAgeTooOld() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(50000)
                .age(76)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Age cannot exceed 75");
    }

    @Test
    void testCreateEmployeeRequestWithValidBoundaryAges() {
        CreateEmployeeRequest youngRequest = CreateEmployeeRequest.builder()
                .name("Young Employee")
                .salary(30000)
                .age(16)
                .title("Intern")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> youngViolations = validator.validate(youngRequest);
        assertThat(youngViolations).isEmpty();

        CreateEmployeeRequest oldRequest = CreateEmployeeRequest.builder()
                .name("Senior Employee")
                .salary(100000)
                .age(75)
                .title("Senior Consultant")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> oldViolations = validator.validate(oldRequest);
        assertThat(oldViolations).isEmpty();
    }

    @Test
    void testCreateEmployeeRequestWithNullAge() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(50000)
                .age(null)
                .title("Software Engineer")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Age is required");
    }

    @Test
    void testCreateEmployeeRequestWithBlankTitle() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(50000)
                .age(30)
                .title("")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title cannot be blank");
    }

    @Test
    void testCreateEmployeeRequestWithNullTitle() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("John Doe")
                .salary(50000)
                .age(30)
                .title(null)
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Title cannot be blank");
    }

    @Test
    void testCreateEmployeeRequestWithMultipleViolations() {
        CreateEmployeeRequest request = CreateEmployeeRequest.builder()
                .name("")
                .salary(-1000)
                .age(10)
                .title("")
                .build();

        Set<ConstraintViolation<CreateEmployeeRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(4);
    }
}
