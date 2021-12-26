create table USER
(
    ID         bigint primary key auto_increment,
    NAME       varchar(10),
    TEL        varchar(20) unique,
    AVATAR_URL varchar(1024),
    CREATED_AT timestamp not null default now(),
    UPDATED_AT timestamp not null default now()
)
