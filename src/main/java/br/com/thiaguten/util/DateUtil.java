package br.com.thiaguten.util;

import java.time.*;

public final class DateUtil {

    public static final ZoneId SAO_PAULO_ZONE_ID = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(-3));

    public static final ZoneId DEFAULT_ZONE_ID =
//            ZoneId.systemDefault();
            SAO_PAULO_ZONE_ID;

    private DateUtil() {
        // not instantiable
    }

    public static ZonedDateTime epochMilliToZonedDateTime(long epochMilli) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), DEFAULT_ZONE_ID);
    }

    public static LocalDateTime epochMilliToLocalDateTime(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), DEFAULT_ZONE_ID);
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
        return zonedDateTimeToEpochMilli(localDateTime.atZone(DEFAULT_ZONE_ID));
    }

    public static long localDateToEpochMilli(LocalDate localDate) {
        return localDateTimeToEpochMilli(localDate.atStartOfDay());
    }

    public static long locaTimeToEpochMilli(LocalTime localTime) {
        return localDateTimeToEpochMilli(localTime.atDate(LocalDate.now()));
    }

}
