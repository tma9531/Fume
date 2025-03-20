create table developed_by
(
    vgnr integer not null
        references video_game,
    dnr  integer not null
        references developer,
    primary key (vgnr, dnr)
);

alter table developed_by
    owner to p32001_11;

