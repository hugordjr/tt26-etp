package com.estapar.parking.api.dto.request;

import com.estapar.parking.domain.enums.EventType;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConditionalValidation.Validator.class)
@Documented
public @interface ConditionalValidation {
  String message() default "Campos obrigatorios invalidos";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class Validator implements ConstraintValidator<ConditionalValidation, WebhookEventRequest> {

    @Override
    public void initialize(ConditionalValidation annotation) {}

    @Override
    public boolean isValid(WebhookEventRequest request, ConstraintValidatorContext context) {
      if (request == null) {
        return true;
      }

      boolean isValid = true;
      context.disableDefaultConstraintViolation();

      if (request.getEventType() == EventType.ENTRY) {
        if (request.getEntryTime() == null) {
          context
              .buildConstraintViolationWithTemplate("entry_time e obrigatorio para eventos ENTRY")
              .addPropertyNode("entryTime")
              .addConstraintViolation();
          isValid = false;
        }
      }

      return isValid;
    }
  }
}
