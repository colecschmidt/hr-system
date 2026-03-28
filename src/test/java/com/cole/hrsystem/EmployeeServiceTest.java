package com.cole.hrsystem;

import com.cole.hrsystem.dto.EmployeeDto;
import com.cole.hrsystem.exception.DuplicateResourceException;
import com.cole.hrsystem.exception.ResourceNotFoundException;
import com.cole.hrsystem.model.Department;
import com.cole.hrsystem.model.Employee;
import com.cole.hrsystem.repository.AppUserRepository;
import com.cole.hrsystem.repository.DepartmentRepository;
import com.cole.hrsystem.repository.EmployeeRepository;
import com.cole.hrsystem.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepository employeeRepo;
    @Mock DepartmentRepository departmentRepo;
    @Mock AppUserRepository userRepo;

    @InjectMocks EmployeeService employeeService;

    private Department dept;
    private EmployeeDto.CreateRequest createReq;

    @BeforeEach
    void setUp() {
        dept = new Department("Engineering", "Software team");
        dept.setId(1L); // normally set by DB

        createReq = new EmployeeDto.CreateRequest();
        createReq.setFirstName("Jane");
        createReq.setLastName("Doe");
        createReq.setEmail("jane@example.com");
        createReq.setJobTitle("Software Engineer");
        createReq.setSalary(new BigDecimal("95000"));
        createReq.setHireDate(LocalDate.now());
        createReq.setDepartmentId(1L);
    }

    @Test
    void create_succeeds_with_valid_request() {
        when(employeeRepo.existsByEmail(any())).thenReturn(false);
        when(departmentRepo.findById(1L)).thenReturn(Optional.of(dept));
        when(employeeRepo.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(42L);
            return e;
        });

        EmployeeDto.Response response = employeeService.create(createReq);

        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getDepartmentName()).isEqualTo("Engineering");
        verify(employeeRepo).save(any(Employee.class));
    }

    @Test
    void create_throws_when_email_already_exists() {
        when(employeeRepo.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(createReq))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("jane@example.com");
    }

    @Test
    void create_throws_when_department_not_found() {
        when(employeeRepo.existsByEmail(any())).thenReturn(false);
        when(departmentRepo.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.create(createReq))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department");
    }

    @Test
    void getById_throws_when_employee_not_found() {
        when(employeeRepo.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee");
    }
}
