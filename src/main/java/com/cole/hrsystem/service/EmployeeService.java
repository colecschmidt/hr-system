package com.cole.hrsystem.service;

import com.cole.hrsystem.dto.EmployeeDto;
import com.cole.hrsystem.exception.ResourceNotFoundException;
import com.cole.hrsystem.exception.DuplicateResourceException;
import com.cole.hrsystem.model.AppUser;
import com.cole.hrsystem.model.Department;
import com.cole.hrsystem.model.Employee;
import com.cole.hrsystem.repository.AppUserRepository;
import com.cole.hrsystem.repository.DepartmentRepository;
import com.cole.hrsystem.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer owns all business logic.
 * Controllers stay thin — they only handle HTTP concerns.
 * This is the same separation you used in OnyxChat's handler/store split.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepo;
    private final DepartmentRepository departmentRepo;
    private final AppUserRepository userRepo;

    // ---- Read ----

    @Transactional(readOnly = true)
    public Page<EmployeeDto.Summary> listAll(Pageable pageable) {
        return employeeRepo.findAll(pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto.Summary> listByDepartment(Long deptId, Pageable pageable) {
        return employeeRepo.findByDepartmentId(deptId, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto.Summary> search(String query, Pageable pageable) {
        return employeeRepo.search(query, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public EmployeeDto.Response getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    // ---- Write ----

    public EmployeeDto.Response create(EmployeeDto.CreateRequest req) {
        if (employeeRepo.existsByEmail(req.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + req.getEmail());
        }

        Department dept = departmentRepo.findById(req.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.getDepartmentId()));

        Employee emp = new Employee();
        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setEmail(req.getEmail());
        emp.setJobTitle(req.getJobTitle());
        emp.setSalary(req.getSalary());
        emp.setHireDate(req.getHireDate());
        emp.setDepartment(dept);

        if (req.getManagerId() != null) {
            AppUser manager = userRepo.findById(req.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Manager not found: " + req.getManagerId()));
            emp.setManager(manager);
        }

        return toResponse(employeeRepo.save(emp));
    }

    public EmployeeDto.Response update(Long id, EmployeeDto.UpdateRequest req) {
        Employee emp = findOrThrow(id);

        if (req.getFirstName()  != null) emp.setFirstName(req.getFirstName());
        if (req.getLastName()   != null) emp.setLastName(req.getLastName());
        if (req.getJobTitle()   != null) emp.setJobTitle(req.getJobTitle());
        if (req.getSalary()     != null) emp.setSalary(req.getSalary());
        if (req.getStatus()     != null) emp.setStatus(req.getStatus());

        if (req.getEmail() != null && !req.getEmail().equals(emp.getEmail())) {
            if (employeeRepo.existsByEmail(req.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + req.getEmail());
            }
            emp.setEmail(req.getEmail());
        }

        if (req.getDepartmentId() != null) {
            Department dept = departmentRepo.findById(req.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + req.getDepartmentId()));
            emp.setDepartment(dept);
        }

        return toResponse(employeeRepo.save(emp));
    }

    public void delete(Long id) {
        employeeRepo.delete(findOrThrow(id));
    }

    // ---- Helpers ----

    private Employee findOrThrow(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    private EmployeeDto.Response toResponse(Employee e) {
        EmployeeDto.Response r = new EmployeeDto.Response();
        r.setId(e.getId());
        r.setFirstName(e.getFirstName());
        r.setLastName(e.getLastName());
        r.setEmail(e.getEmail());
        r.setJobTitle(e.getJobTitle());
        r.setSalary(e.getSalary());
        r.setHireDate(e.getHireDate());
        r.setStatus(e.getStatus());
        r.setCreatedAt(e.getCreatedAt());
        if (e.getDepartment() != null) r.setDepartmentName(e.getDepartment().getName());
        if (e.getManager()    != null) r.setManagerName(e.getManager().getUsername());
        return r;
    }

    private EmployeeDto.Summary toSummary(Employee e) {
        EmployeeDto.Summary s = new EmployeeDto.Summary();
        s.setId(e.getId());
        s.setFirstName(e.getFirstName());
        s.setLastName(e.getLastName());
        s.setEmail(e.getEmail());
        s.setJobTitle(e.getJobTitle());
        s.setStatus(e.getStatus());
        if (e.getDepartment() != null) s.setDepartmentName(e.getDepartment().getName());
        return s;
    }
}
