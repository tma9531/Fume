create table contained_in
(
    cnr  integer not null
        references collection,
    vgnr integer not null
        references video_game,
    primary key (cnr, vgnr)
);

alter table contained_in
    owner to p32001_11;

