package br.com.thiaguten.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateUtil {

    private DateUtil() {
        // not instantiable
    }

    public static ZonedDateTime epochMilliToZonedDateTime(long epochMilli) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    public static LocalDateTime epochMilliToLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
    }

    public static LocalDate epochMilliToLocalDate(long epochMilli) {
        return epochMilliToLocalDateTime(epochMilli).toLocalDate();
    }

    public static LocalTime epochMilliToLocalTime(long epochMilli) {
        return epochMilliToLocalDateTime(epochMilli).toLocalTime();
    }

    public static long zonedDateTimeToEpochMilli(ZonedDateTime zonedDateTime) {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public static long localDateTimeToEpochMilli(LocalDateTime localDateTime) {
        return zonedDateTimeToEpochMilli(localDateTime.atZone(ZoneId.systemDefault()));
    }

    public static long localDateToEpochMilli(LocalDate localDate) {
        return localDateTimeToEpochMilli(localDate.atStartOfDay());
    }

    public static long locaTimeToEpochMilli(LocalTime localTime) {
        return localDateTimeToEpochMilli(localTime.atDate(LocalDate.now()));
    }

}
