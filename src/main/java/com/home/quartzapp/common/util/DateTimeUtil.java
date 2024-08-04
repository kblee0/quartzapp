package com.home.quartzapp.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtil {
    public static Date toDate(LocalDateTime dt) {
        return dt == null ? null : java.util.Date.from(dt.atZone(ZoneId.systemDefault()).toInstant());
    }
    public static Date toDate(LocalDate dt) {
        return dt == null ? null : java.util.Date.from(dt.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
    public static LocalDateTime toLocalDateTime(Date dt) {
        return dt == null ? null : LocalDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault());
    }
    public static LocalDate toLocalDate(Date dt) {
        return dt == null ? null : LocalDate.ofInstant(dt.toInstant(), ZoneId.systemDefault());
    }
}
