package com.estapar.parking.api.dto.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

  private static final DateTimeFormatter[] FORMATTERS = {
    DateTimeFormatter.ISO_OFFSET_DATE_TIME,
    DateTimeFormatter.ISO_DATE_TIME,
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  };

  @Override
  public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException {
    String dateString = p.getText().trim();

    for (DateTimeFormatter formatter : FORMATTERS) {
      try {
        if (formatter == FORMATTERS[FORMATTERS.length - 1]) {
          LocalDateTime localDateTime = LocalDateTime.parse(dateString, formatter);
          return localDateTime.atOffset(ZoneOffset.UTC);
        } else {
          return OffsetDateTime.parse(dateString, formatter);
        }
      } catch (DateTimeParseException e) {
        continue;
      }
    }

    throw new IOException(
        "Não foi possível fazer parse da data: " + dateString
            + ". Formatos suportados: yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX/Z]");
  }
}
