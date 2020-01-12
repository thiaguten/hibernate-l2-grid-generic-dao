package br.com.thiaguten.converter;

import br.com.thiaguten.util.DateUtil;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts LocalTime type to String type and vice-versa.
 * 
 * @author Thiago Gutenberg Carvalho da Costa
 */
@Converter
public class LocalTimeConverter implements AttributeConverter<LocalTime, String> {

	// This class is immutable and thread-safe.
	public static final DateTimeFormatter WRITE_FORMATTER = DateTimeFormatter.ofPattern("HH.mm.ss")
			.withZone(DateUtil.DEFAULT_ZONE_ID);

	// This class is immutable and thread-safe.
	public static final DateTimeFormatter READ_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
			.withZone(DateUtil.DEFAULT_ZONE_ID);

	@Override
	public String convertToDatabaseColumn(LocalTime attribute) {
		return WRITE_FORMATTER.format(attribute);
	}

	@Override
	public LocalTime convertToEntityAttribute(String dbData) {
		try {
			return LocalTime.parse(dbData, READ_FORMATTER);
		} catch (DateTimeParseException e) {
			return LocalTime.parse(dbData, WRITE_FORMATTER);
		}
	}

}
