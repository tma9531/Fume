create table collection
(
    cnr      integer default nextval('collection_cnr_seq'::regclass) not null
        primary key,
    name     varchar(255)                                            not null,
    username varchar(255)                                            not null
        references users
);

alter table collection
    owner to p32001_11;
