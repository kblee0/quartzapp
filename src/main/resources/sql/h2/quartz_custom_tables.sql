create table QRTZ_JOB_HISTORY
(
    SCHED_NAME    VARCHAR(120) not null,
    ENTRY_ID      VARCHAR(95)  not null,
    TRIGGER_NAME  VARCHAR(190) not null,
    TRIGGER_GROUP VARCHAR(190) not null,
    JOB_NAME      VARCHAR(190),
    JOB_GROUP     VARCHAR(190),
    JOB_DATA      VARCHAR(4096),
    START_TIME    TIMESTAMP              not null,
    END_TIME      TIMESTAMP,
    STATUS        VARCHAR(16)  not null,
    EXIT_MESSAGE  VARCHAR(2500),
    primary key (SCHED_NAME, ENTRY_ID)
);

