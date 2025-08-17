package nl.andarabski.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "application")  // geen conflict, maar maakt het consistent
public class Application implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
  //  @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-applications")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name = "vacancy_id")
    @JsonBackReference(value = "vacancy-applications")
    private Vacancy vacancy;
    private String motivation;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;
    private Date appliedAt;

    public Application() {}

    public Application(Long id, User user, Vacancy vacancy, String motivation, ApplicationStatus status, Date appliedAt) {
        this.id = id;
        this.user = user;
        this.vacancy = vacancy;
        this.motivation = motivation;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public Vacancy getVacancy() {
        return vacancy;
    }

    public void setVacancy(Vacancy vacancy) {
        this.vacancy = vacancy;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Date getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Date appliedAt) {
        this.appliedAt = appliedAt;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", user=" + user +
                ", vacancy=" + vacancy +
                ", motivation='" + motivation + '\'' +
                ", status='" + status + '\'' +
                ", appliedAt=" + appliedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
