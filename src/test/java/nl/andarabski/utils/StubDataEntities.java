package nl.andarabski.utils;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StubDataEntities {

    List<Application> listApplications;
    Application app1, app2, app3;
    public List<Application> getListApplications() {

        this.listApplications = new ArrayList();

        User mockUser = new User();
        mockUser.setEmail("johndoe@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPassword("pas123");
        mockUser.setAge(21);
        mockUser.setPhoto("src/main/resources/mock/vlinder.jpeg");
        mockUser.setCv("src/main/resources/mock/Cv_Three.pdf");
        mockUser.setRole("user");
        mockUser.setEnabled(true);

        Vacancy mockVacancy = new Vacancy();
        mockVacancy.setTitle("Java Developer");
        mockVacancy.setCompanyName("IBM");
        mockVacancy.setDescription("Looking for a skilled Java developer.");
        mockVacancy.setLocation("Amsterdam");
        mockVacancy.setCompanyName("TechCorp");

        app1 = new Application();
        //  application1.setUser(mockUser);
        //  application1.setVacancy(mockVacancy);
        app1.setMotivation("We will contact you soon.");
        app1.setStatus(ApplicationStatus.APPLIED);
        app1.setAppliedAt(new Date());
        this.listApplications.add(app1);

        app2 = new Application();
        //application2.setUser(mockUser);
        //application2.setVacancy(mockVacancy);
        app2.setMotivation("We will contact you soon.");
        app2.setStatus(ApplicationStatus.PENDING);
        app2.setAppliedAt(new Date());
        this.listApplications.add(app2);

        app3 = new Application();
        // application3.setUser(new User());
        // application3.setVacancy(new Vacancy());
        app3.setMotivation("We will contact you soon.");
        app3.setStatus(ApplicationStatus.REJECTED);
        app3.setAppliedAt(new Date());
        this.listApplications.add(app3);

        return this.listApplications;
    }
}
