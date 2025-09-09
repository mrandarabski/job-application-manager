package nl.andarabski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.ApplyRequestDto;
import nl.andarabski.dto.MotivationRequestDto;
import nl.andarabski.mapper.ApplicationMapper;
import nl.andarabski.model.Application;
import nl.andarabski.service.ApplicationService;
import nl.andarabski.system.Result;
import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
//@RequestMapping("/api/v1/applications")
@RequestMapping("${api.endpoint.base-url}/applications")

public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;

    @GetMapping("/{applicationId}")
    public Result findById(@PathVariable Long applicationId) {
        ApplicationDto dto = this.applicationService.findById(applicationId);
        if (dto == null) {
            throw new ObjectNotFoundException("Application not found", applicationId );
        }
        return new Result(true, StatusCode.SUCCESS, "Find One Success", dto);
    }

    @GetMapping()
    public Result findAllVacancies() {
        List<ApplicationDto> applications = this.applicationService.findAll();
       return new Result(true, StatusCode.SUCCESS, "Find All Success", applications);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/add")
    public Result create(@Valid @RequestBody ApplicationDto applicationDto) {
        var saved = applicationService.create(applicationDto);
        return new Result(true, StatusCode.CREATED, "Application created successfully", saved);
    }


   @PutMapping("/{applicationId}")
    public Result updateApplication(@PathVariable Long applicationId, @RequestBody ApplicationDto applicationDto) {
        // Converter DTO naar entity
        ApplicationDto updated = applicationService.update(applicationId, applicationDto);
       return new Result(true, StatusCode.SUCCESS, "Application updated successfully", updated);
    }

    @DeleteMapping("/{applicationId}")
    public Result deleteApplication(@PathVariable Long applicationId) {
        this.applicationService.delete(applicationId);
       return new Result(true, StatusCode.SUCCESS, "Delete Success");

    }

    // NB: service retourneert Application (entity) → map naar DTO voor response
    @PostMapping("/users/{userId}/vacancies/{vacancyId}/apply")
    public ResponseEntity<Result> apply(
            @PathVariable Long userId,
            @PathVariable Long vacancyId,
            @RequestBody MotivationRequestDto request
    ) {
        if (request == null || request.getMotivation() == null || request.getMotivation().isBlank()) {
            Result err = new Result(false, StatusCode.INVALID_ARGUMENT, "Invalid motivation", null);
            return ResponseEntity.badRequest().body(err); // HTTP 400
        }

        Application application = applicationService.applyToVacancy(userId, vacancyId, request.getMotivation());
        ApplicationDto dto = applicationMapper.toDto(application);

        Result ok = new Result(true, StatusCode.CREATED, "Application submitted successfully", dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ok); // HTTP 201
    }


    @PostMapping("/apply")
    public Result applyToVacancy(@RequestBody ApplyRequestDto request) {
        try {
            Application application = applicationService.applyToVacancy(
                    request.getUserId(),
                    request.getVacancyId(),
                    request.getMotivation()
            );

            ApplicationDto applicationDto = applicationMapper.toDto(application);
            return new Result(true, StatusCode.SUCCESS, "Application submitted successfully", applicationDto);

        } catch (IllegalArgumentException e) {
            return new Result(false, StatusCode.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (ObjectNotFoundException e) {
            return new Result(false, StatusCode.NOT_FOUND, e.getMessage());
        }
    }
}

