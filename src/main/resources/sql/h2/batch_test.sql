create table T_BATCH_IN
(
    BATCH_ID    NUMBER(10),
    BATCH_NAME  VARCHAR(255),
    START_DT    DATETIME,
    END_DT      DATETIME,
    REC_CNT     NUMBER(10),
    constraint T_BATCH_IN_PK
        primary key (BATCH_ID)
);

create table T_BATCH_OUT
(
    CREATE_DT   DATETIME,
    BATCH_ID    NUMBER(10),
    START_DT    DATETIME,
    END_DT      DATETIME,
    REC_CNT     NUMBER(10),
    OUT_CNT     NUMBER(10)
);