package com.cole.hrsystem.service;

import com.cole.hrsystem.exception.DuplicateResourceException;
import com.cole.hrsystem.exception.ResourceNotFoundException;
import com.cole.hrsystem.model.Department;
import com.cole.hrsystem.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepo;

    @Transactional(readOnly = true)
    public List<Department> listAll() {
        return departmentRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Department getById(Long id) {
        return departmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    public Department create(String name, String description) {
        if (departmentRepo.existsByName(name)) {
            throw new DuplicateResourceException("Department already exists: " + name);
        }
        return departmentRepo.save(new Department(name, description));
    }

    public Department update(Long id, String name, String description) {
        Department dept = getById(id);
        if (name != null) dept.setName(name);
        if (description != null) dept.setDescription(description);
        return departmentRepo.save(dept);
    }

    public void delete(Long id) {
        departmentRepo.delete(getById(id));
    }
}
