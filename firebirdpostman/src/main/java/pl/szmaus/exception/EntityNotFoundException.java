package pl.szmaus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code= HttpStatus.NOT_FOUND, reason = "Id has not been found")
public class EntityNotFoundException extends RuntimeException {

}
