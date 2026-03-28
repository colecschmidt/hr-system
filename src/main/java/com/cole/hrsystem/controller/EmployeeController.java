package com.cole.hrsystem.controller;

import com.cole.hrsystem.dto.EmployeeDto;
import com.cole.hrsystem.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controllers are intentionally thin — they translate HTTP to service calls.
 * No business logic lives here.
 *
 * Role guards mirror OnyxChat's AdminOnly middleware, just declarative instead of imperative.
 *   ADMIN   — full CRUD
 *   MANAGER — read + update
 *   EMPLOYEE — read only
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<Page<EmployeeDto.Summary>> list(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "lastName") Pageable pageable) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(employeeService.search(search, pageable));
        }
        if (departmentId != null) {
            return ResponseEntity.ok(employeeService.listByDepartment(departmentId, pageable));
        }
        return ResponseEntity.ok(employeeService.listAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<EmployeeDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto.Response> create(@Valid @RequestBody EmployeeDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(req));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<EmployeeDto.Response> update(
            @PathVariable Long id,
            @RequestBody EmployeeDto.UpdateRequest req) {
        return ResponseEntity.ok(employeeService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
