package nl.andarabski.repository;

import nl.andarabski.model.Application;
import nl.andarabski.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.applications WHERE u.id = :id")
    Optional<User> findByIdWithApplications(@Param("id") Long id);

}
