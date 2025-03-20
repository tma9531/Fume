create table rates
(
    username varchar(255) not null
        constraint fk_rates_username
            references users,
    vgnr     integer      not null
        constraint fk_rates_vgnr
            references video_game,
    rating   integer      not null
        constraint chk_rating
            check ((rating >= 1) AND (rating <= 5)),
    primary key (username, vgnr)
);

alter table rates
    owner to p32001_11;
