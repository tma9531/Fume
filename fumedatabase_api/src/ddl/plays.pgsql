create table plays
(
    start_timestamp timestamp not null
        primary key,
    end_timestamp   timestamp not null,
    username        varchar(255)
        references users,
    vgnr            integer
        references video_game
);

alter table plays
    owner to p32001_11;

