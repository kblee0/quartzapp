package com.home.quartzapp.common.config;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class DateTimeConfig {
    private final static String DATETIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    private final static String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private final static String TIME_FORMAT_STRING = "HH:mm:ss";

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule
                .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING)))
                .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING)))
                .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)))
                .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING)))
                .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_FORMAT_STRING)))
                .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_FORMAT_STRING)));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(javaTimeModule)
                        .setDateFormat(new SimpleDateFormat(DATETIME_FORMAT_STRING));

        return objectMapper;
    }

//    @Bean
//    Formatter<LocalDateTime> localDateTimeFormatter() {
//        return new Formatter<>() {
//            @Override
//            public String print(LocalDateTime object, Locale locale) {
//                return DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING).format(object);
//            }
//
//            @Override
//            public LocalDateTime parse(String text, Locale locale) {
//                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING, locale));
//            }
//        };
//    }
//
//    @Bean
//    Formatter<LocalDate> localDateFormatter() {
//        return new Formatter<>() {
//            @Override
//            public String print(LocalDate object, Locale locale) {
//                return DateTimeFormatter.ofPattern(DATE_FORMAT_STRING).format(object);
//            }
//
//            @Override
//            public LocalDate parse(String text, Locale locale) {
//                return LocalDate.parse(text, DateTimeFormatter.ofPattern(DATE_FORMAT_STRING, locale));
//            }
//        };
//    }
//
//    @Bean
//    Formatter<LocalTime> localTimeFormatter() {
//        return new Formatter<>() {
//            @Override
//            public String print(LocalTime object, Locale locale) {
//                return DateTimeFormatter.ofPattern(TIME_FORMAT_STRING).format(object);
//            }
//
//            @Override
//            public LocalTime parse(String text, Locale locale) {
//                return LocalTime.parse(text, DateTimeFormatter.ofPattern(TIME_FORMAT_STRING, locale));
//            }
//        };
//    }
//
//    @Bean
//    Formatter<Date> dateFormatter() {
//        return new Formatter<>() {
//            @Override
//            public String print(Date object, Locale locale) {
//                return new SimpleDateFormat(DATETIME_FORMAT_STRING).format(object);
//            }
//
//            @Override
//            public Date parse(String text, Locale locale) throws ParseException {
//                return new SimpleDateFormat(DATETIME_FORMAT_STRING, locale).parse(text);
//            }
//        };
//    }
}
