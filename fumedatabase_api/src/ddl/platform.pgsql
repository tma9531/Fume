create table platform
(
    pfnr integer default nextval('platform_name_seq'::regclass) not null
        primary key,
    name varchar(255)
);

alter table platform
    owner to p32001_11;

