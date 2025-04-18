package com.home.quartzapp.scheduler.constant;

import org.quartz.Trigger;
import org.quartz.impl.jdbcjobstore.Constants;

public enum TriggerType {
    CRON,
    SIMPLE,
    FIXED,
    ONCE;

    public static String getDataMapName() {
        return "triggerType";
    }
    public boolean equals(String value) {
        return this.name().equals(value);
    }
    public boolean equals(Trigger trigger) {
        if (trigger == null) { return false; }
        return this.name().equals(getTriggerType(trigger));
    }

    public static final String getTriggerType(Trigger trigger) {
        if (trigger == null) { return null; }
        if (trigger.getJobDataMap() == null) { return null; }
        return trigger.getJobDataMap().getString(getDataMapName());
    }

    public static final boolean isEqualTriggerType(Trigger t1, Trigger t2) {
        if (getTriggerType(t1) == null) { return false; }
        return getTriggerType(t1).equals(getTriggerType(t2));
    }
}
