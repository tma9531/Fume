create table users
(
    username       varchar(255)              not null
        constraint "User_pkey"
            primary key,
    password       varchar(255)              not null
        constraint chk_password
            check ((length((password)::text) >= 8) AND ((password)::text ~ '[A-Z]'::text) AND
                   ((password)::text ~ '[0-9]'::text)),
    creationdate   date default CURRENT_DATE not null,
    lastaccessdate date default CURRENT_DATE not null
);

alter table users
    owner to p32001_11;

