package br.com.thiaguten.converter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts LocalDate type to String type and vice-versa.
 * 
 * @author Thiago Gutenberg Carvalho da Costa
 */
@Converter
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

	public static final DateTimeFormatter WRITE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
			.withZone(ZoneId.systemDefault());

	public static final DateTimeFormatter READ_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
			.withZone(ZoneId.systemDefault());

	@Override
	public String convertToDatabaseColumn(LocalDate attribute) {
		return WRITE_FORMATTER.format(attribute);
	}

	@Override
	public LocalDate convertToEntityAttribute(String dbData) {
		try {
			return LocalDate.parse(dbData, READ_FORMATTER);
		} catch (DateTimeParseException e) {
			return LocalDate.parse(dbData, WRITE_FORMATTER);
		}
	}

}
