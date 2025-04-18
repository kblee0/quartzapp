create table QRTZ_JOB_HISTORY
(
    SCHED_NAME    VARCHAR(120) not null,
    JOB_NAME      VARCHAR(200) not null,
    JOB_GROUP     VARCHAR(200) not null,
    START_TIME    TIMESTAMP  not null,
    END_TIME      TIMESTAMP,
    TRIGGER_NAME  VARCHAR(200) not null,
    TRIGGER_GROUP VARCHAR(200) not null,
    JOB_DATA      VARCHAR(4096),
    STATUS        VARCHAR(16)  not null,
    EXIT_CODE     VARCHAR(2500),
    EXIT_MESSAGE  VARCHAR(2500),
    primary key (SCHED_NAME, JOB_NAME, JOB_GROUP, START_TIME)
);

