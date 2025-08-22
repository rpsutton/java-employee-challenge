package com.reliaquest.api.model;

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
public class CreateEmployeeRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be greater than zero")
    private Integer salary;

    @NotNull(message = "Age is required")
    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age cannot exceed 75")
    private Integer age;

    @NotBlank(message = "Title cannot be blank")
    private String title;
}