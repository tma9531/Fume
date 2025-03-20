create table video_game
(
    vgnr       integer default nextval('video_game_vgnr_seq'::regclass) not null
        constraint videogame_pkey
            primary key,
    title      varchar(255)                                             not null,
    esrbrating varchar(255)
        constraint chk_esrb_rating
            check ((esrbrating)::text = ANY
                   ((ARRAY ['C'::character varying, 'E'::character varying, 'E10+'::character varying, 'T'::character varying, 'M'::character varying, 'AO'::character varying, 'RP'::character varying])::text[]))
);

alter table video_game
    owner to p32001_11;
