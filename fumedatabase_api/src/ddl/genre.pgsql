create table genre
(
    gnr  integer default nextval('genre_gnr_seq'::regclass) not null
        primary key,
    type varchar(255)                                       not null
);

alter table genre
    owner to p32001_11;

