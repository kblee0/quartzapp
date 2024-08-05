package com.home.quartzapp.common.config;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.format.Formatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class DateTimeConfig {
    private final static String DATETIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    private final static String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private final static String TIME_FORMAT_STRING = "HH:mm:ss";

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new
                JsonSerializer<>() {
                    @Override
                    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                            throws IOException {
                        gen.writeString(DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING).format(value));
                    }
                }
        );
        javaTimeModule.addSerializer(LocalDate.class, new
                JsonSerializer<>() {
                    @Override
                    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
                            throws IOException {
                        gen.writeString(DateTimeFormatter.ofPattern(DATE_FORMAT_STRING).format(value));
                    }
                }
        );
        javaTimeModule.addSerializer(LocalTime.class, new
                JsonSerializer<>() {
                    @Override
                    public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers)
                            throws IOException {
                        gen.writeString(DateTimeFormatter.ofPattern(TIME_FORMAT_STRING).format(value));
                    }
                }
        );
        javaTimeModule.addSerializer(Date.class, new
                JsonSerializer<>() {
                    @Override
                    public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers)
                            throws IOException {
                        gen.writeString(new SimpleDateFormat(DATETIME_FORMAT_STRING).format(value));
                    }
                }
        );
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return LocalDateTime.parse(p.getValueAsString(), DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING));
            }
        });
        javaTimeModule.addDeserializer(LocalDate.class, new JsonDeserializer<>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return LocalDate.parse(p.getValueAsString(), DateTimeFormatter.ofPattern(DATE_FORMAT_STRING));
            }
        });
        javaTimeModule.addDeserializer(LocalTime.class, new JsonDeserializer<>() {
            @Override
            public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return LocalTime.parse(p.getValueAsString(), DateTimeFormatter.ofPattern(TIME_FORMAT_STRING));
            }
        });
        javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<>() {
            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                try {
                    return new SimpleDateFormat(DATETIME_FORMAT_STRING).parse(p.getValueAsString());
                } catch (ParseException e) {
                    throw new JsonParseException(p, e.getMessage(), e);
                }
            }
        });
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    @Bean
    Formatter<LocalDateTime> localDateTimeFormatter() {
        return new Formatter<>() {
            @Override
            public String print(LocalDateTime object, Locale locale) {
                return DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING).format(object);
            }

            @Override
            public LocalDateTime parse(String text, Locale locale) {
                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(DATETIME_FORMAT_STRING, locale));
            }
        };
    }

    @Bean
    Formatter<LocalDate> localDateFormatter() {
        return new Formatter<>() {
            @Override
            public String print(LocalDate object, Locale locale) {
                return DateTimeFormatter.ofPattern(DATE_FORMAT_STRING).format(object);
            }

            @Override
            public LocalDate parse(String text, Locale locale) {
                return LocalDate.parse(text, DateTimeFormatter.ofPattern(DATE_FORMAT_STRING, locale));
            }
        };
    }

    @Bean
    Formatter<LocalTime> localTimeFormatter() {
        return new Formatter<>() {
            @Override
            public String print(LocalTime object, Locale locale) {
                return DateTimeFormatter.ofPattern(TIME_FORMAT_STRING).format(object);
            }

            @Override
            public LocalTime parse(String text, Locale locale) {
                return LocalTime.parse(text, DateTimeFormatter.ofPattern(TIME_FORMAT_STRING, locale));
            }
        };
    }

    @Bean
    Formatter<Date> dateFormatter() {
        return new Formatter<>() {
            @Override
            public String print(Date object, Locale locale) {
                return new SimpleDateFormat(DATETIME_FORMAT_STRING).format(object);
            }

            @Override
            public Date parse(String text, Locale locale) throws ParseException {
                return new SimpleDateFormat(DATETIME_FORMAT_STRING, locale).parse(text);
            }
        };
    }
}
