package nl.andarabski.system;

import nl.andarabski.model.Application;
import nl.andarabski.model.ApplicationStatus;
import nl.andarabski.model.User;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.repository.UserRepository;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DBDatainitializer implements CommandLineRunner {


    private final UserRepository userRepository;
    private final VacancyRepository vacancyRepository;
    private final ApplicationRepository repository;
    private final UserService userService;
    private List<Application> listApplications;

    public DBDatainitializer(UserRepository userRepository, VacancyRepository vacancyRepository, ApplicationRepository repository, UserService userService, List<Application> listApplications) {

        this.userRepository = userRepository;
        this.vacancyRepository = vacancyRepository;
        this.repository = repository;
        this.userService = userService;
        this.listApplications = listApplications;
    }
    @Override
    public void run(String... args) throws Exception {

        User user1 = new User();
        user1.setFirstName("Andre");
        user1.setLastName("Dabski");
        user1.setEmail("test@gmail.com");
        user1.setPassword("12345");
        user1.setAge(25);
        user1.setPhoto("src/main/resources/mock/leeuw.jpeg");
        user1.setCv("src/main/resources/mock/Cv_One.pdf");
        user1.setRole("admin");
        user1.setEnabled(true);
        user1.setApplications(listApplications);


        User user2 = new User();
        user2.setFirstName("John");
        user2.setLastName("Johnson");
        user2.setEmail("test@gmail.com");
        user2.setPassword("54321");
        user2.setAge(25);
        user2.setPhoto("src/main/resources/mock/natuur.jpeg");
        user2.setCv("src/main/resources/mock/Cv_Two.pdf");
        user2.setRole("admin user");
        user2.setEnabled(true);
        user2.setApplications(listApplications);

        User user3 = new User();
        user3.setFirstName("Sonny");
        user3.setLastName("Andarabski");
        user3.setEmail("test@gmail.com");
        user3.setPassword("12345");
        user3.setAge(21);
        user3.setPhoto("src/main/resources/mock/vlinder.jpeg");
        user3.setCv("src/main/resources/mock/Cv_Three.pdf");
        user3.setRole("user");
        user3.setEnabled(true);
        user3.setApplications(listApplications);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);

        Vacancy vacancy1 = new Vacancy();
        vacancy1.setTitle("Java Developer");
        vacancy1.setCompanyName("IBM");
        vacancy1.setDescription("Java Developer");
        vacancy1.setLocation("Amsterdam");
        vacancy1.setPostedAt(new java.util.Date());
        vacancy1.setApplications(listApplications);

        Vacancy vacancy2 = new Vacancy();
        vacancy2.setTitle("Python Developer");
        vacancy2.setCompanyName("Microsoft");
        vacancy2.setDescription("Python Developer");
        vacancy2.setLocation("Rotterdam");
        vacancy2.setPostedAt(new java.util.Date());
        vacancy2.setApplications(listApplications);

        Vacancy vacancy3 = new Vacancy();
        vacancy3.setTitle("C# Developer");
        vacancy3.setCompanyName("Oracle");
        vacancy3.setDescription("C# Developer");
        vacancy3.setLocation("Utrecht");
        vacancy3.setPostedAt(new java.util.Date());
        vacancy3.setApplications(listApplications);

        vacancy1 = vacancyRepository.save(vacancy1);
        vacancy2 = vacancyRepository.save(vacancy2);
        vacancy3 = vacancyRepository.save(vacancy3);

        Application application1 = new Application();
        application1.setUser(user1);
        application1.setVacancy(vacancy1);
        application1.setMotivation("We will contact you soon.");
        application1.setStatus(ApplicationStatus.APPLIED);
        application1.setAppliedAt(new java.util.Date());

        Application application2 = new Application();
        application2.setUser(user2);
        application2.setVacancy(vacancy2);
        application2.setMotivation("We will contact you soon.");
        application2.setStatus(ApplicationStatus.PENDING);
        application2.setAppliedAt(new java.util.Date());

        Application application3 = new Application();
        application3.setUser(user3);
        application3.setVacancy(vacancy3);
        application3.setMotivation("We will contact you soon.");
        application3.setStatus(ApplicationStatus.REJECTED);
        application3.setAppliedAt(new java.util.Date());

        listApplications.add(application1);
        listApplications.add(application2);
        listApplications.add(application3);

        repository.save(application1);
        repository.save(application2);
        repository.save(application3);

    }
}
