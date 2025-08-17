package nl.andarabski.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(String objectName, String id) {
        super("Could not find " + objectName + " with Id: " + id + " :(");
    }

    public ObjectNotFoundException(String objectName, Long id) {
        super("Could not find " + objectName + " with Id: " + id + " :(");
    }
}