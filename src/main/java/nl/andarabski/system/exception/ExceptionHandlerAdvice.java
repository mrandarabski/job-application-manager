package nl.andarabski.system.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import nl.andarabski.system.Result;
import nl.andarabski.system.StatusCode;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    // 400 — Bean Validation (@Valid) fouten op @RequestBody DTO’s
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return new Result(false, StatusCode.INVALID_ARGUMENT, "Validation failed", errors);
    }

    // 400 — Bind/Type problemen bij form-data
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBindException(BindException ex) {
        String msg = ex.getAllErrors().stream()
                .map(e -> e.getDefaultMessage() == null ? e.toString() : e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new Result(false, StatusCode.INVALID_ARGUMENT, msg, null);
    }

    // 400 — Ontbrekende verplichte parameter
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMissingParam(MissingServletRequestParameterException ex) {
        return new Result(false, StatusCode.INVALID_ARGUMENT,
                "Missing request parameter: " + ex.getParameterName(), null);
    }

    // 400 — Type mismatch, onleesbare JSON velden, etc.
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            InvalidFormatException.class,
            HttpMessageNotReadableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleTypeMismatch(Exception ex) {
        return new Result(false, StatusCode.INVALID_ARGUMENT, "Invalid request payload", ex.getMessage());
    }

    // 400 — Upload gerelateerde fouten (te groot/beschadigd)
    @ExceptionHandler({ MaxUploadSizeExceededException.class, MultipartException.class, HttpMediaTypeNotSupportedException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleUploadErrors(Exception ex) {
        return new Result(false, StatusCode.INVALID_ARGUMENT, "Invalid upload/content type", ex.getMessage());
    }

    // 404 — Domeinobject niet gevonden
    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNotFound(ObjectNotFoundException ex) {
        return new Result(false, StatusCode.NOT_FOUND, ex.getMessage(), null);
    }

    // 501 (in jouw StatusCode benoemd als CONFLICT) — business/dataconflicten
    @ExceptionHandler({ IllegalArgumentException.class, DataIntegrityViolationException.class })
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public Result handleConflict(Exception ex) {
        return new Result(false, StatusCode.CONFLICT, ex.getMessage(), null);
    }

    // 500 — Fallback
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleOtherException(Exception ex) {
        return new Result(false, StatusCode.INTERNAL_SERVER_ERROR,
                "A server internal error occurs.", ex.getMessage());
    }
}
