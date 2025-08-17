package nl.andarabski.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "vacancy")  // geen conflict, maar goed om expliciet te maken
public class Vacancy implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String title;
    private String companyName;
    private String description;
    private String location;
    private Date postedAt;
    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "vacancy-applications")
    private List<Application> applications;


    public Vacancy() {}

    public Vacancy(Long id, String title, String  companyName, String description, String location, Date postedAt,
                   List<Application> applications) {
        this.id = id;
        this.title = title;
        this.companyName = companyName;
        this.description = description;
        this.location = location;
        this.postedAt = postedAt;
        this.applications = applications;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() { return companyName; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Date postedAt) {
        this.postedAt = postedAt;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public void addApplication(Application application) {
        application.setVacancy(this);
        applications.add(application);
    }
    public void removeApplication(Application applicationTeBeRemoved) {
        applicationTeBeRemoved.setVacancy(null);
        this.applications.remove(applicationTeBeRemoved);
    }
    public void removeAllApplications() {
        this.applications.stream().forEach(application -> application.setVacancy(null));
        this.applications = new java.util.ArrayList<>();
    }

    @Override
    public String toString() {
        return "Vacancy{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", companyName='" + companyName + '\'' +
                ", description='" + description + '\'' +
                ", location='" + location + '\'' +
                ", postedAt=" + postedAt +
                ", applications=" + applications +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vacancy vacancy = (Vacancy) o;
        return Objects.equals(id, vacancy.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
