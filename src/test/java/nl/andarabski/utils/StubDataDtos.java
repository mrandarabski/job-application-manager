package nl.andarabski.utils;

import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.model.Application;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public  class StubDataDtos {

    List<Application> listEntities;
    List<ApplicationDto> listApplications;
    ApplicationDto appDto1, appDto2, appDto3;
    public List<ApplicationDto> getListApplications() {

        this.listApplications = new ArrayList();
        listEntities = new ArrayList<>();

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

        Date posted = new Date();

        Vacancy mockVacancy = new Vacancy();
        mockVacancy.setTitle("Java Developer");
        mockVacancy.setCompanyName("IBM");
        mockVacancy.setDescription("Looking for a skilled Java developer.");
        mockVacancy.setLocation("Amsterdam");

        mockVacancy.setPostedAt(posted);
        mockVacancy.setApplications(listEntities);

        appDto1 = new ApplicationDto();
        appDto1.setMotivation("We will contact you soon.");
        appDto1.setStatus("APPLIED");
        appDto1.setAppliedAt(posted);
        this.listApplications.add(appDto1);

        appDto2 = new ApplicationDto();
        appDto2.setMotivation("We will contact you soon.");
        appDto2.setStatus("PENDING");
        appDto2.setAppliedAt(posted);
        this.listApplications.add(appDto2);

        appDto3 = new ApplicationDto();
       // application3.setUser(new User());
       // application3.setVacancy(new Vacancy());
        appDto3.setMotivation("We will contact you soon.");
        appDto3.setStatus("REJECTED");
        appDto3.setAppliedAt(posted);
        this.listApplications.add(appDto3);

        return listApplications;
    }
}


