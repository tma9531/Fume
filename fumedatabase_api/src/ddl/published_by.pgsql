create table published_by
(
    vgnr integer not null
        references video_game,
    pnr  integer not null
        references publisher,
    primary key (vgnr, pnr)
);

alter table published_by
    owner to p32001_11;
