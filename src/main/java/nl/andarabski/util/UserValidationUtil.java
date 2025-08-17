package nl.andarabski.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.andarabski.dto.ApplicationDto;
import nl.andarabski.system.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;

public class UserValidationUtil {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    public static Result validateFile(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            return new Result(false, 400, "Bestand voor '" + fieldName + "' ontbreekt", null);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return new Result(false, 400, "Bestand '" + file.getOriginalFilename() + "' is groter dan 5MB", null);
        }
        return null;
    }

    public static Result validateJson(String json, String fieldName) {
        if (json == null || json.trim().isEmpty()) {
            return new Result(false, 400, "JSON-waarde voor '" + fieldName + "' ontbreekt", null);
        }
        return null;
    }

    public static Result validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return new Result(false, 400, "Ongeldig e-mailadres", null);
        }
        return null;
    }

    public static Result validatePassword(String password) {
        if (password == null || password.length() < 5) {
            return new Result(false, 400, "Wachtwoord moet minimaal 5 tekens zijn", null);
        }
        return null;
    }

    public static Result validateAge(int age) {
        if (age < 0 || age > 150) {
            return new Result(false, 400, "Leeftijd moet tussen 0 en 150 zijn", null);
        }
        return null;
    }

    public static List<ApplicationDto> parseApplicationsJson(String applicationsJson) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(applicationsJson, new TypeReference<>() {});
    }
}