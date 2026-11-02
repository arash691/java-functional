package shopflow.api.w11.catalog;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * One place that turns exceptions into RFC 9457 problem responses (application/problem+json).
 * Extending ResponseEntityExceptionHandler already gives you ProblemDetail bodies for Spring's own exceptions.
 * EX-11-01: ProductNotFoundException -> 404.  EX-11-03: validation errors -> 400 with an "errors" list.
 * SF-07: DuplicateSkuException -> 409.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
}
