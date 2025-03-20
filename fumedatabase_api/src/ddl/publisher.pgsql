create table publisher
(
    pnr  integer default nextval('publisher_pnr_seq'::regclass) not null
        primary key,
    name varchar(255)                                           not null
);

alter table publisher
    owner to p32001_11;

