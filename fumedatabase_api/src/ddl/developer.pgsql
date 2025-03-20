create table developer
(
    dnr  integer default nextval('developer_dnr_seq'::regclass) not null
        primary key,
    name varchar(255)                                           not null
);

alter table developer
    owner to p32001_11;

