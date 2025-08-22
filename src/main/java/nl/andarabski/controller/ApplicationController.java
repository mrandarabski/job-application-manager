package nl.andarabski.controller;

import jakarta.validation.Valid;
import nl.andarabski.converter.ApplicationDtoToApplicationConverter;
import nl.andarabski.converter.ApplicationToApplicationDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.ApplyRequestDto;
import nl.andarabski.dto.MotivationRequestDto;
import nl.andarabski.model.Application;
import nl.andarabski.repository.ApplicationRepository;
import nl.andarabski.service.ApplicationService;
import nl.andarabski.system.Result;
import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final ApplicationToApplicationDtoConverter applicationToApplicationDtoConverter;
    private final ApplicationDtoToApplicationConverter applicationDtoToApplicationConverter;


    public ApplicationController(ApplicationRepository applicationRepository, ApplicationService applicationService, ApplicationToApplicationDtoConverter applicationToApplicationDtoConverter, ApplicationDtoToApplicationConverter applicationDtoToApplicationConverter) {
        this.applicationRepository = applicationRepository;
        this.applicationService = applicationService;
        this.applicationToApplicationDtoConverter = applicationToApplicationDtoConverter;
        this.applicationDtoToApplicationConverter = applicationDtoToApplicationConverter;
    }

   /* @GetMapping("/{applicationId}")
    public Result findById(@PathVariable Long applicationId){
        Application foudApplication = this.applicationService.findById(applicationId);
        ApplicationDto applicationDto = this.applicationToApplicationDtoConverter.convert(foudApplication);
       return new Result(true, StatusCode.SUCCESS, "Find Success", applicationDto);
    }*/

    @GetMapping
    public Result findAllVacancies(){
        List<ApplicationDto> applications = this.applicationService.findAll();
        return new Result(true, StatusCode.SUCCESS, "Find All Success", applications);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/add", produces = "application/json")
    public Result createUser(@Valid @RequestBody ApplicationDto applicationDto ){
        Application newApplication = this.applicationDtoToApplicationConverter.convert(applicationDto);
        Application savedApplication = this.applicationRepository.save(newApplication);
        ApplicationDto savedApplicationDto = this.applicationToApplicationDtoConverter.convert(savedApplication);
        return new Result(true, StatusCode.SUCCESS, "Create Success", savedApplicationDto);
    }

    @PutMapping("/{applicationId}")
    public Result updateVacancy(@PathVariable Long applicationId, @RequestBody ApplicationDto applicationDto){
        // Converter DTO naar entity
        Application update = this.applicationDtoToApplicationConverter.convert(applicationDto);
        // Update in service
        Application updateApplication = this.applicationService.update(applicationId, update);
        // Converteer terug naar DTO
        ApplicationDto savedApplicationDto = this.applicationToApplicationDtoConverter.convert(updateApplication);
        return new Result(true, StatusCode.SUCCESS, "Update Success", savedApplicationDto);
    }

    @DeleteMapping("/{applicationId}")
    public Result deleteVacancy(@PathVariable Long applicationId){
        this.applicationService.delete(applicationId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");

    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyToVacancy(@RequestBody ApplyRequestDto request) {
        try {
            Application application = applicationService.applyToVacancy(
                    request.getUserId(),
                    request.getVacancyId(),
                    request.getMotivation()
            );

            ApplicationDto applicationDto = applicationToApplicationDtoConverter.convert(application);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Result(true, StatusCode.SUCCESS, "Application submitted successfully", applicationDto));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Result(false, StatusCode.INTERNAL_SERVER_ERROR, e.getMessage()));
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new Result(false, StatusCode.NOT_FOUND, e.getMessage()));
        }
    }

    @PostMapping("/users/{userId}/vacancies/{vacancyId}/apply")
    public ResponseEntity<Result> apply(@PathVariable Long userId,
                                        @PathVariable Long vacancyId,
                                        @Valid @RequestBody MotivationRequestDto request) {

        // optioneel, maar met goede annotaties in DTO meestal overbodig
        if (request.getMotivation().trim().isEmpty()) {
            throw new IllegalArgumentException("Motivation is required");
        }

        Application application = applicationService.applyToVacancy(userId, vacancyId, request.getMotivation());
        ApplicationDto dto = applicationToApplicationDtoConverter.convert(application);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new Result(true, StatusCode.SUCCESS, "Application submitted successfully", dto)
        );
    }

   /* @PostMapping("/users/{userId}/vacancies/{vacancyId}/apply")
    public ResponseEntity<Result> apply(@PathVariable Long userId,
                                        @PathVariable Long vacancyId,
                                        @Valid @RequestBody MotivationRequestDto request) {

        if (request == null || request.getMotivation() == null || request.getMotivation().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    new Result(false, StatusCode.ERROR, "Motivation is required", null));
        }

        try {
            Application application = applicationService.applyToVacancy(userId, vacancyId, request.getMotivation());
            ApplicationDto dto = applicationToApplicationDtoConverter.convert(application);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new Result(true, StatusCode.SUCCESS, "Application submitted successfully", dto)
            );

        } catch (ObjectNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new Result(false, StatusCode.NOT_FOUND, ex.getMessage(), null)
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new Result(false, StatusCode.CONFLICT, ex.getMessage(), null)
            );

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Result(false, StatusCode.ERROR, "Unexpected error occurred", null)
            );
        }
    }*/

}
