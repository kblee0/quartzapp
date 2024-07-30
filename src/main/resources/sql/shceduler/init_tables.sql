create table T_LOGIN_USER
(
    ID       BIGINT            not null,
    LOGIN_ID CHARACTER VARYING not null,
    PASSWORD CHARACTER VARYING,
    NAME     CHARACTER VARYING,
    EMAIL    CHARACTER VARYING,
    ROLES    CHARACTER VARYING,
    constraint T_LOGIN_USER_PK
        primary key (ID)
);

create unique index T_LOGIN_USER_ID_IX01
    on T_LOGIN_USER (ID, LOGIN_ID);

