package nl.andarabski.controller;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vacancies")
public class VacancyController {

    private final VacancyRepository vacancyRepository;
    private final VacancyService vacancyService;
    private final VacancyToVacancyDtoConverter vacancyToVacancyDtoConverter;
    private final VacancyDtoToVacancyConverter vacancyDtoToVacancyConverter;


    public VacancyController(VacancyRepository vacancyRepository, VacancyService vacancyService, VacancyToVacancyDtoConverter vacancyToVacancyDtoConverter, VacancyDtoToVacancyConverter vacancyDtoToVacancyConverter) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyService = vacancyService;
        this.vacancyToVacancyDtoConverter = vacancyToVacancyDtoConverter;
        this.vacancyDtoToVacancyConverter = vacancyDtoToVacancyConverter;
    }

    @GetMapping("/{vacacyId}")
    public Result findById(@PathVariable Long vacacyId){
        VacancyDto vacancyFound = this.vacancyService.findById(vacacyId);
       // VacancyDto vacancyDto = this.vacancyToVacancyDtoConverter.convert(vacancyFound);

        return new Result(true, StatusCode.SUCCESS, "Find One Success", vacancyFound);
    }

    @GetMapping
    public Result findAllVacancies(){
        List<VacancyDto> listVacancies = this.vacancyService.findAll();
        return new Result(true, StatusCode.SUCCESS, "Find All Success", listVacancies);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/add", produces = "application/json")
    public Result createVacancy(@Valid @RequestBody VacancyDto vacancyDto){
        Vacancy newVacancy = this.vacancyDtoToVacancyConverter.convert(vacancyDto);
        Vacancy savedVacancy = this.vacancyService.save(newVacancy);
        VacancyDto savedVacancyDto  = this.vacancyToVacancyDtoConverter.convert(savedVacancy);
        return new Result(true, StatusCode.SUCCESS, "Create Success", savedVacancyDto);
    }

    @PutMapping("/{vacancyId}")
    public Result updateVacancy(@PathVariable Long vacancyId, @RequestBody VacancyDto vacancyDto){
        Vacancy update = this.vacancyDtoToVacancyConverter.convert(vacancyDto);
        Vacancy updatedVacancy = this.vacancyService.update(vacancyId ,update);
        VacancyDto updatedVacancyDto = this.vacancyToVacancyDtoConverter.convert(updatedVacancy);
        return new Result(true, StatusCode.SUCCESS, "Update Success", updatedVacancyDto);
    }

    @DeleteMapping("/{vacancyId}")
    public Result deleteVacancy(@PathVariable Long vacancyId){
        this.vacancyService.delete(vacancyId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }
}
