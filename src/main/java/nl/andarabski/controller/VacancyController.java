package nl.andarabski.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.andarabski.converter.VacancyDtoToVacancyConverter;
import nl.andarabski.converter.VacancyToVacancyDtoConverter;
import nl.andarabski.dto.VacancyDto;
import nl.andarabski.model.Vacancy;
import nl.andarabski.repository.VacancyRepository;
import nl.andarabski.service.VacancyService;
import nl.andarabski.system.Result;
import nl.andarabski.system.StatusCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.endpoint.base-url}/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    @GetMapping("/{vacancyId}")
    public Result findById(@PathVariable("vacancyId") Long vacancyId){
        VacancyDto vacancyFound = this.vacancyService.findById(vacancyId);
        return new Result(true, StatusCode.SUCCESS, "Find One Success", vacancyFound);
    }

    @GetMapping
    public Result findAllVacancies(){
        List<VacancyDto> listVacancies = this.vacancyService.findAll();
        return new Result(true, StatusCode.SUCCESS, "Find All Success", listVacancies);
    }

    @PostMapping(value="/add",
            consumes=MediaType.APPLICATION_JSON_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result> createVacancy(@Valid @RequestBody VacancyDto vacancyDto) {
        VacancyDto saved = vacancyService.create(vacancyDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Result(true, StatusCode.CREATED, "Vacancy created successfully", saved));
    }


    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Result update(@PathVariable Long id, @Valid @RequestBody VacancyDto dto) {
        VacancyDto updated = vacancyService.update(id, dto); // DTO in/uit
        return new Result(true, StatusCode.SUCCESS, "Update Success", updated);
    }


 /*   @PutMapping("/{vacancyId}")
    public Result updateVacancy(@PathVariable Long vacancyId, @RequestBody VacancyDto vacancyDto){
        Vacancy update = this.vacancyDtoToVacancyConverter.convert(vacancyDto);
        Vacancy updatedVacancy = this.vacancyService.update(vacancyId ,update);
        VacancyDto updatedVacancyDto = this.vacancyToVacancyDtoConverter.convert(updatedVacancy);
        return new Result(true, StatusCode.SUCCESS, "Update Success", updatedVacancyDto);
    }*/

    @DeleteMapping("/{vacancyId}")
    public Result deleteVacancy(@PathVariable Long vacancyId){
        this.vacancyService.delete(vacancyId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }
}
