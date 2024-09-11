package com.home.quartzapp.quartzjobs.util;

import com.home.quartzapp.common.exception.ErrorCodeException;
import lombok.Getter;
import org.quartz.JobDataMap;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class JobDataMapWrapper {
    private final JobDataMap jobDataMap;
    private final LocalDateTime now;

    public JobDataMapWrapper() {
        this.jobDataMap = new JobDataMap();
        this.now = LocalDateTime.now();
    }

    public JobDataMapWrapper(JobDataMap jobDataMap) {
        this.jobDataMap = jobDataMap;
        this.now = LocalDateTime.now();
        jobDataMap.forEach((key, value) -> {
            if(value instanceof String) this.jobDataMap.put(key, this.nowFunConvert((String)value));
        });
    }

    public JobDataMapWrapper(Map<String, Object> jobDataMap) {
        this.jobDataMap = new JobDataMap(jobDataMap);
        this.now = LocalDateTime.now();
        jobDataMap.forEach((key, value) -> {
            if(value instanceof String) this.jobDataMap.put(key, this.nowFunConvert((String)value));
        });
    }

    public static JobDataMapWrapper create() {
        return new JobDataMapWrapper();
    }

    public <T> Optional<T> get(String key, Class<T> clazz) {
        if(jobDataMap == null) return Optional.empty();
        Object obj;
        try {
            obj= jobDataMap.get(key);
        } catch (Exception e) {
            obj = null;
        }
        if (obj == null || (obj instanceof String && !StringUtils.hasText((String)obj))) return Optional.empty();

        try {
            return Optional.of((T)obj);
        } catch (Exception e) {
            Exception cause = new ClassCastException("The object identified by the key name '" + key + "' is not a " + clazz.getSimpleName().toLowerCase());
            throw new ErrorCodeException("QJBE0001", cause, key);
        }
    }

    protected <T> Optional<T> getParseValue(String key, Function<String,? extends T> parser, Class<T> clazz) {
        if(jobDataMap == null) return Optional.empty();
        Object obj;
        try {
            obj= jobDataMap.get(key);
        } catch (Exception e) {
            obj = null;
        }
        if (obj == null) return Optional.empty();

        try {
            if(obj instanceof String) {
                return StringUtils.hasText((String)obj) ? Optional.of(parser.apply((String)obj)) : Optional.empty();
            } else {
                return Optional.of((T) obj);
            }
        } catch (Exception e) {
            Exception cause = new ClassCastException("The object identified by the key name '" + key + "' is not a " + clazz.getSimpleName().toLowerCase());
            throw new ErrorCodeException("QJBE0001", cause, key);
        }
    }

    public Optional<String> getString(String key) {
        return get(key, String.class);
    }

    public Optional<Integer> getInteger(String key) {
        return getParseValue(key, Integer::parseInt, Integer.class);
    }
    public Optional<Long> getLong(String key) {
        return getParseValue(key, Long::parseLong, Long.class);
    }
    public Optional<Double> getDouble(String key) {
        return getParseValue(key, Double::parseDouble, Double.class);
    }
    public Optional<Boolean> getBoolean(String key) {
        return getParseValue(key, Boolean::parseBoolean, Boolean.class);
    }

    public JobDataMapWrapper put(String key, Object value) {
        jobDataMap.put(key, value);
        return this;
    }

    public String nowFunConvert(String template) {
        Matcher matcher = Pattern.compile("\\$\\{now(:([^\\{\\}#]*?)|())\\}").matcher(template);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String format = matcher.group(2);
            String formattedStr = format == null || format.isEmpty() ?
                    now.toString() :
                    now.format(DateTimeFormatter.ofPattern(format));
            matcher.appendReplacement(sb, formattedStr);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
