package com.home.quartzapp.quartzjobs.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class JobDataMapWrapperTest {

    @Test
    void nowFunConvert() {
        Map<String, Object> map = new HashMap<>() {{
            put("key1", "${now}");
            put("key2", "${now:yyyy-MM-dd HH:mm:ss}");
            put("key3", "${now yyyy-MM-dd HH:mm:ss}");
        }};
        JobDataMapWrapper wrapper = new JobDataMapWrapper(map);
        assertThat(wrapper.getString("key1").get()).isNotEqualTo("${now}");
        assertThat(wrapper.getString("key2").get()).isNotEqualTo("${now:yyyy-MM-dd HH:mm:ss}");
        assertThat(wrapper.getString("key3").get()).isEqualTo("${now yyyy-MM-dd HH:mm:ss}");
    }
}