package nl.andarabski.testsupport;

import nl.andarabski.model.*;
import nl.andarabski.dto.*;
import java.time.Instant;
import java.util.*;

public final class TD {
    private TD() {}

    // deterministische tijd voor alle tests
    public static final Instant FIXED_INSTANT = Instant.parse("2025-08-15T12:00:00Z");
    public static final Date FIXED_DATE = Date.from(FIXED_INSTANT);

    // ---------- ENTITIES ----------

    public static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setFirstName("Andre");
        u.setLastName("Dabski");
        u.setEmail("test+" + id + "@gmail.com");
        u.setPassword("secret");
        u.setRole("admin");
        u.setEnabled(true);
        u.setApplications(new ArrayList<>());
        return u;
    }

    public static Vacancy vacancy(long id) {
        Vacancy v = new Vacancy();
        v.setId(id);
        v.setTitle("Java Developer");
        v.setCompanyName("Acme");
        v.setDescription("We are looking for an experienced Java Developer");
        v.setLocation("Amsterdam");
        v.setPostedAt(FIXED_DATE);
        v.setApplications(new ArrayList<>());
        return v;
    }

    public static Application application(long id, User user, Vacancy vacancy,
                                          ApplicationStatus status, String motivation) {
        Application a = new Application();
        a.setId(id);
        a.setMotivation(motivation);
        a.setStatus(status);
        a.setAppliedAt(FIXED_DATE);
        link(a, user, vacancy);
        return a;
    }

    /** Koppel app <-> user/vacancy bidirectioneel (null-safe). */
    public static void link(Application app, User user, Vacancy vacancy) {
        app.setUser(user);
        app.setVacancy(vacancy);
        if (user.getApplications() == null) user.setApplications(new ArrayList<>());
        if (vacancy.getApplications() == null) vacancy.setApplications(new ArrayList<>());
        if (!user.getApplications().contains(app)) user.getApplications().add(app);
        if (!vacancy.getApplications().contains(app)) vacancy.getApplications().add(app);
    }

    // ---------- DTOS ----------

    public static UserDto userDto(long id) {
        UserDto d = new UserDto();
        d.setId(id);
        d.setFirstName("Andre");
        d.setLastName("Dabski");
        d.setEmail("test+" + id + "@gmail.com");
        d.setPassword("secret");
        d.setRole("admin");
        d.setApplications(new ArrayList<>());
        return d;
    }

    public static VacancyDto vacancyDto(long id) {
        VacancyDto d = new VacancyDto();
        d.setId(id);
        d.setTitle("Java Developer");
        d.setCompanyName("Acme");
        d.setDescription("We are looking for an experienced Java Developer");
        d.setLocation("Amsterdam");
        d.setPostedAt(FIXED_DATE);
        d.setApplications(new ArrayList<>());
        return d;
    }

    public static ApplicationDto applicationDto(long id, long userId, long vacancyId,
                                                String status, String motivation) {
        ApplicationDto d = new ApplicationDto();
        d.setId(id);
        d.setUserId(userId);
        d.setVacancyId(vacancyId);
        d.setStatus(status);
        d.setMotivation(motivation);
        d.setAppliedAt(FIXED_DATE);
        return d;
    }
}

