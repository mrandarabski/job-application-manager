package nl.andarabski.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Date;
import java.util.List;

public class VacancyDto {

    Long id;
    @NotEmpty(message = "Title is required.")
    String title;
    @NotEmpty(message = "Company name is required")
    String companyName;
    @NotEmpty(message = "Description is required.")
    String description;
    @NotEmpty(message = "Location is required.")
    String location;
    Date postedAt;
    List<ApplicationDto> applications;

    public VacancyDto() {}

    public VacancyDto(Long id, String title, String companyName, String description, String location, Date postedAt, List<ApplicationDto> apps) {
        this.id = id;
        this.title = title;
        this.companyName = companyName;
        this.description = description;
        this.location = location;
        this.postedAt = postedAt;
        this.applications = apps;
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

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

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

    public List<ApplicationDto> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationDto> applications) {
        this.applications = applications;
    }

}
