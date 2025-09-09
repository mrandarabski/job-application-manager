package nl.andarabski.repository;

import nl.andarabski.model.Application;
import nl.andarabski.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // gebruikt de paden Application.user.id en Application.vacancy.id
    boolean existsByUserIdAndVacancyId(Long userId, Long vacancyId);

    // (optioneel) ook handig:
    Optional<Application> findByUserIdAndVacancyId(Long userId, Long vacancyId);

}
