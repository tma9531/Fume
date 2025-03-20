create table is_genre
(
    vgnr integer
        references video_game,
    gnr  integer
        references genre
);

alter table is_genre
    owner to p32001_11;
