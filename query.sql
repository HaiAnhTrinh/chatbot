create table USER
(
    FB_ID      VARCHAR,
    EMAIL      VARCHAR not null,
    GIVEN_NAME VARCHAR not null,
    LAST_NAME  VARCHAR not null,
    INDICATOR  INT default 0,
    constraint USER_PK
        primary key (EMAIL)
);

create table ROOM
(
    CAPACITY INT     default 1             not null,
    NAME     VARCHAR default RANDOM_UUID() not null,
    constraint ROOM_PK
        primary key (NAME)
);

create table ROOM_AVAILABILITY
(
    ROOM_NAME    VARCHAR            not null,
    DATE         DATE               not null,
    AVAILABILITY CHAR(96) default 0 not null,
    constraint ROOM_AVAILABILITY_ROOM_NAME_FK
        foreign key (ROOM_NAME) references ROOM (NAME)
            on update cascade on delete cascade
);