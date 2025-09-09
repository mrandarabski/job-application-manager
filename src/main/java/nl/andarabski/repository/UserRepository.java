package nl.andarabski.repository;

import nl.andarabski.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
//    @EntityGraph(attributePaths = {"applications", "applications.vacancy"})
//    Optional<User> findById(Long id);
//
//    // Voor findAll (alleen oké bij kleine datasets; liever Page<>):
//    @EntityGraph(attributePaths = {"applications", "applications.vacancy"})
//    List<User> findAll();
//
//    // Beter: pagineren
//    @EntityGraph(attributePaths = {"applications", "applications.vacancy"})
//    Page<User> findAll(Pageable pageable);
}


// extra safeguard