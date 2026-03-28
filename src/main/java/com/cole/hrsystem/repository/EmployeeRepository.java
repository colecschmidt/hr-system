package com.cole.hrsystem.repository;

import com.cole.hrsystem.model.Employee;
import com.cole.hrsystem.model.Employee.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA gives you CRUD for free.
 * You only write methods for custom queries.
 * This maps directly to your Go store pattern — same idea, less boilerplate.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    // Paginated list filtered by department
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    // Paginated list filtered by status
    Page<Employee> findByStatus(EmploymentStatus status, Pageable pageable);

    // Employees managed by a specific manager
    Page<Employee> findByManagerId(Long managerId, Pageable pageable);

    // Search by name (case-insensitive) — useful for a search endpoint
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(e.email)     LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Employee> search(@Param("query") String query, Pageable pageable);
}
