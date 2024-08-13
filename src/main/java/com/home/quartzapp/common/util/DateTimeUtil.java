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
    public static <T> T max(T t1, T t2) {
        if(t1 == null) return t2;
        if(t2 == null) return t1;

        return ((Comparable<T>)t1).compareTo(t2) > 0 ? t1 : t2;
    }
    public static <T> T min(T t1, T t2) {
        if(t1 == null) return t2;
        if(t2 == null) return t1;

        return ((Comparable<T>)t1).compareTo(t2) > 0 ? t2 : t1;
    }
}
