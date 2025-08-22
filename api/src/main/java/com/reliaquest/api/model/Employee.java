package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {
    private String id;

    @JsonProperty("employee_name")
    @NotBlank(message = "Name cannot be blank", groups = CreateValidation.class)
    private String employeeName;

    @JsonProperty("employee_salary")
    @NotNull(message = "Salary is required", groups = CreateValidation.class)
    @Positive(message = "Salary must be greater than zero", groups = CreateValidation.class)
    private Integer employeeSalary;

    @JsonProperty("employee_age")
    @NotNull(message = "Age is required", groups = CreateValidation.class)
    @Min(value = 16, message = "Age must be at least 16", groups = CreateValidation.class)
    @Max(value = 75, message = "Age cannot exceed 75", groups = CreateValidation.class)
    private Integer employeeAge;

    @JsonProperty("employee_title")
    @NotBlank(message = "Title cannot be blank", groups = CreateValidation.class)
    private String employeeTitle;

    @JsonProperty("employee_email")
    private String employeeEmail;
}