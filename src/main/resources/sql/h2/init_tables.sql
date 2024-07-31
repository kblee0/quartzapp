create table T_LOGIN_USER
(
    ID       BIGINT       not null,
    LOGIN_ID VARCHAR(255) not null,
    PASSWORD VARCHAR(255),
    NAME     VARCHAR(255),
    EMAIL    VARCHAR(255),
    ROLES    VARCHAR(255),
    constraint T_LOGIN_USER_PK
        primary key (ID)
);

create unique index T_LOGIN_USER_ID_IX01
    on T_LOGIN_USER (ID, LOGIN_ID);

