create table T_LOGIN_USER
(
    USER_ID  VARCHAR(64)  not null,
    LOGIN_ID VARCHAR(255) not null,
    PASSWORD VARCHAR(255),
    NAME     VARCHAR(255),
    EMAIL    VARCHAR(255),
    ROLES    VARCHAR(255),
    REFRESH_TOKEN VARCHAR(1024),
    constraint T_LOGIN_USER_PK
        primary key (USER_ID)
);

create unique index T_LOGIN_USER_ID_IX01
    on T_LOGIN_USER (LOGIN_ID);

insert into T_LOGIN_USER (USER_ID, LOGIN_ID, PASSWORD, NAME, EMAIL, ROLES, REFRESH_TOKEN) values ('0', 'admin', '$2a$12$aRc0Bqr6KQVE7jefhz14AutNxVMNrWl5aylez3TlsMnvKuImx8Nti', '어드민', 'admin@gmail.com', 'ROLE_ADMIN,ROLE_USER,ROLE_MANAGER', '');
insert into T_LOGIN_USER (USER_ID, LOGIN_ID, PASSWORD, NAME, EMAIL, ROLES, REFRESH_TOKEN) values ('232323', 'cuser', '$2a$12$aRc0Bqr6KQVE7jefhz14AutNxVMNrWl5aylez3TlsMnvKuImx8Nti', '사용자', 'cuser@gmail.com', 'ROLE_USER', '');
insert into T_LOGIN_USER (USER_ID, LOGIN_ID, PASSWORD, NAME, EMAIL, ROLES, REFRESH_TOKEN) values ('343434', 'cman', '$2a$12$aRc0Bqr6KQVE7jefhz14AutNxVMNrWl5aylez3TlsMnvKuImx8Nti', '메니저', 'cman@gmail.com', 'ROLE_MANAGER,ROLE_USER', null);
