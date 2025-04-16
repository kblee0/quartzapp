package com.home.quartzapp.scheduler.constant;

import org.quartz.Trigger;
import org.quartz.impl.jdbcjobstore.Constants;

public class TriggerType {
    public static final String TTYPE_CRON = Constants.TTYPE_CRON;
    public static final String TTYPE_SIMPLE = Constants.TTYPE_SIMPLE;
    public static final String TTYPE_FIXED = "FIXED";
    public static final String TTYPE_ONCE = "ONCE";
    public static final String TTYPE_DATAMAP_NAME = "triggerType";

    public static final String getTriggerType(Trigger trigger) {
        if (trigger == null) { return null; }
        if (trigger.getJobDataMap() == null) { return null; }
        return trigger.getJobDataMap().getString(TTYPE_DATAMAP_NAME);
    }
    public static final boolean isEqualTriggerType(Trigger t1, Trigger t2) {
        if (t1 == null) { return false; }
        return getTriggerType(t1).equals(getTriggerType(t2));
    }
    public static final boolean isCronTriggerType(Trigger trigger) {
        return TTYPE_CRON.equals(getTriggerType(trigger));
    }
    public static final boolean isSimpleTriggerType(Trigger trigger) {
        return TTYPE_SIMPLE.equals(getTriggerType(trigger));
    }
    public static final boolean isFixedTriggerType(Trigger trigger) {
        return TTYPE_FIXED.equals(getTriggerType(trigger));
    }
    public static final boolean isOnceTriggerType(Trigger trigger) {
        return TTYPE_ONCE.equals(getTriggerType(trigger));
    }
}
