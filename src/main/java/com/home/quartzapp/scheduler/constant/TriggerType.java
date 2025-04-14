package com.home.quartzapp.scheduler.constant;

import org.quartz.impl.jdbcjobstore.Constants;

public class TriggerType {
    public static final String TTYPE_CRON = Constants.TTYPE_CRON;
    public static final String TTYPE_SIMPLE = Constants.TTYPE_SIMPLE;
    public static final String TTYPE_FIXED = "FIXED";
    public static final String TTYPE_ONCE = "ONCE";
    public static final String TTYPE_DATAMAP_NAME = "TriggerType";
}
