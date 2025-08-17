package nl.andarabski.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")  // voorkomt conflict met reserved keyword "user"
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotEmpty(message = "firstname is required.")
    private String firstName;
    @NotEmpty(message = "lastname is required.")
    private String lastName;
    @NotEmpty(message = "email is required.")
    private String email;
    @NotEmpty(message = "password is required.")
    private String password;
    private int age;
    private String photo;
    private String cv;
    @NotEmpty(message = "role is required.")
    private String role;
    private boolean enabled;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "user-applications")
    //@JsonManagedReference
    private List<Application> applications;

    public User() {
    }

    public User(Long id, String firstName, String lastName, String email, String password,
                int age, String photo, String cv, String role, boolean enabled, List<Application> applications) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.age = age;
        this.photo = photo;
        this.cv = cv;
        this.role = role;
        this.enabled = enabled;
        this.applications = applications;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getRole() {
        return role;
    }
    public boolean isEnabled() {
        return enabled;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public void addApplication(Application application) {
        application.setUser(this);
        applications.add(application);
    }
    public void removeApplication(Application applicationTeBeRemoved) {
        applicationTeBeRemoved.setUser(null);
        this.applications.remove(applicationTeBeRemoved);

    }
    public void removeAllApplications() {
        this.applications.stream().forEach(application -> application.setUser(null));
        this.applications = new ArrayList<>();
       // applications.clear();
    }

}

/*
The file above is not in your working directory, and will be unavailable to your teammates when you share the request. You can either set up your working directory in Settings, or upload the file to Postman.
 */