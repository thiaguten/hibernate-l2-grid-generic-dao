package br.com.thiaguten.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converts LocalDateTime type to String type and vice-versa.
 * 
 * @author Thiago Gutenberg Carvalho da Costa
 */
@Converter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

	public static final DateTimeFormatter WRITE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS")
			.withZone(ZoneId.systemDefault());

	public static final DateTimeFormatter READ_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
			.withZone(ZoneId.systemDefault());

	@Override
	public String convertToDatabaseColumn(LocalDateTime attribute) {
		return WRITE_FORMATTER.format(attribute);
	}

	@Override
	public LocalDateTime convertToEntityAttribute(String dbData) {
		try {
			String timestampFormatado = String.format("%-26s", dbData.replace(' ', '@')).replace(' ', '0')
					.replace('@', ' ').replace("0000000", ".000000");
			return LocalDateTime.parse(timestampFormatado, READ_FORMATTER);
		} catch (DateTimeParseException e) {
			return LocalDateTime.parse(dbData, WRITE_FORMATTER);
		}
	}

}
