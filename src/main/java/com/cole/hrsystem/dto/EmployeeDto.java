package com.cole.hrsystem.dto;

import com.cole.hrsystem.model.Employee.EmploymentStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTOs keep your API contract separate from your database model.
 * This is a pattern enterprise interviewers will specifically ask about.
 */
public class EmployeeDto {

    // ---- Inbound (what the client sends) ----

    @Data
    public static class CreateRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @Email(message = "Must be a valid email")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Job title is required")
        private String jobTitle;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
        private BigDecimal salary;

        @NotNull(message = "Hire date is required")
        private LocalDate hireDate;

        @NotNull(message = "Department ID is required")
        private Long departmentId;

        private Long managerId; // optional
    }

    @Data
    public static class UpdateRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String jobTitle;
        private BigDecimal salary;
        private EmploymentStatus status;
        private Long departmentId;
        private Long managerId;
    }

    // ---- Outbound (what the server returns) ----

    @Data
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String jobTitle;
        private BigDecimal salary;
        private LocalDate hireDate;
        private EmploymentStatus status;
        private String departmentName;
        private String managerName;
        private LocalDateTime createdAt;
    }

    // Lightweight version for list endpoints (no salary, no timestamps)
    @Data
    public static class Summary {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String jobTitle;
        private EmploymentStatus status;
        private String departmentName;
    }
}
