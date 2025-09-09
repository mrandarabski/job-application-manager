package nl.andarabski.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import nl.andarabski.converter.UserDtoToUserConverter;
import nl.andarabski.converter.UserToUserDtoConverter;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.dto.UserDto;
import nl.andarabski.mapper.UserMapper;
import nl.andarabski.model.User;
import nl.andarabski.service.UserService;
import nl.andarabski.system.Result;
import nl.andarabski.system.StatusCode;
import nl.andarabski.system.exception.ObjectNotFoundException;
import nl.andarabski.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
//@RequestMapping("${api.endpoint.base-url}/applications")
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public Result findUserById(@PathVariable Long id) {
        UserDto dto = userService.findById(id);
        if (dto == null) {
            throw new ObjectNotFoundException("User with id " + id + " not found");
        }
        return new Result(true, StatusCode.SUCCESS, "Find One Success", dto);
    }

    @GetMapping
    public Result findAllUsers() {
        List<UserDto> foundUsers = this.userService.findAll();
        return new Result(true, StatusCode.SUCCESS, "Find All Success", foundUsers);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value="/add", consumes=MediaType.MULTIPART_FORM_DATA_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Result> createUser(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam int age,
            @RequestParam String role,
            @RequestParam boolean enabled,
            @RequestParam(value="photo", required=false) MultipartFile photo,
            @RequestParam(value="cv", required=false) MultipartFile cv,
            @RequestParam(value="applications", required=false) String applicationsJson
    ) throws IOException {

        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new Result(false, StatusCode.INVALID_ARGUMENT, "Photo is required"));
        }

        UserDto dto = new UserDto();
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setAge(age);
        dto.setRole(role);
        dto.setEnabled(enabled);

        dto.setApplications(parseApplicationsSafe(applicationsJson)); // zie helper onderaan

        UserDto saved = userService.create(dto, photo, cv);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new Result(true, StatusCode.CREATED, "User created successfully", saved));
    }



    @PutMapping(value="/update/{id}",
            consumes=MediaType.MULTIPART_FORM_DATA_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public Result updateUser(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam int age,
            @RequestParam String role,
            @RequestParam boolean enabled,
            @RequestParam(value="photo", required=false) MultipartFile photo,
            @RequestParam(value="cv", required=false) MultipartFile cv,
            @RequestParam(value="applications", required=false) String applicationsJson
    ) throws IOException {

        UserDto patch = new UserDto();
        patch.setId(id); // handig voor mappers/service
        patch.setFirstName(firstName);
        patch.setLastName(lastName);
        patch.setEmail(email);
        patch.setPassword(password);
        patch.setAge(age);
        patch.setRole(role);
        patch.setEnabled(enabled);
        patch.setApplications(parseApplicationsSafe(applicationsJson));

        UserDto updated = userService.update(id, patch, photo, cv);
        return new Result(true, StatusCode.SUCCESS, "Update success", updated);
    }


    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable("id") Long id) {
        // Laat service een ObjectNotFoundException gooien wanneer het ID niet bestaat;
        // de advice map't dat naar 404 wat de test verwacht.
        userService.delete(id);
        return new Result(true, StatusCode.SUCCESS, "Delete success", null);
    }

    private List<ApplicationDto> parseApplicationsSafe(String json) {
        if (json == null || json.isBlank()) return java.util.Collections.emptyList();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<ApplicationDto>>() {});
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

}
