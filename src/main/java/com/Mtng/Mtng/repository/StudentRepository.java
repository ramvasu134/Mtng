package com.Mtng.Mtng.repository;

import com.Mtng.Mtng.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Student entity.
 *
 * <p>Performance notes:</p>
 * <ul>
 *   <li>Read-only methods annotated with @QueryHints(readOnly=true) hint Hibernate
 *       to skip dirty-checking for loaded entities, reducing memory overhead.</li>
 *   <li>countByStatus() avoids loading all entities just to get a count.</li>
 *   <li>Pageable overloads allow callers to avoid full-table loads on large datasets.</li>
 * </ul>
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Optional<Student> findByUsername(String username);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Student> findByStatus(String status);

    /** Efficient count – avoids loading entities just for sizing. */
    long countByStatus(String status);

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<Student> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String name, String username);

    boolean existsByUsername(String username);

    /** Pageable version for future large-dataset support. */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Page<Student> findAll(@NonNull Pageable pageable);

    /** All students sorted by name – covers the common dashboard listing. */
    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    @Query("SELECT s FROM Student s ORDER BY s.name ASC")
    List<Student> findAllOrderedByName();
}
