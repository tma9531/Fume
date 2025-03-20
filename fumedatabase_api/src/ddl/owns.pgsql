create table owns
(
    username varchar(255) not null
        references users,
    pfnr     integer      not null
        references platform
);

alter table owns
    owner to p32001_11;

