package nl.andarabski.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;

import java.util.Date;

public class ApplicationDto{

    Long id;
    @NotNull(message = "userId mag niet null zijn")
    Long userId;
    @NotNull(message = "vacancyId mag niet null zijn")
    Long vacancyId;
    @NotEmpty(message = "Motivation is required.")
    String motivation;
    @NotEmpty(message = "Status is required.")
    String status;
    Date appliedAt;

    public ApplicationDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVacancyId() {
        return vacancyId;
    }

    public void setVacancyId(Long vacancyId) {
        this.vacancyId = vacancyId;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Date appliedAt) {
        this.appliedAt = appliedAt;
    }

}
