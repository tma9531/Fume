-- auto-generated definition
create table available_on
(
    game_release_date date,
    game_price        integer,
    vgnr              integer
        references video_game,
    pfnr              integer
        references platform
);

alter table available_on
    owner to p32001_11;

