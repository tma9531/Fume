create table follows
(
    userfollowing     varchar(255) not null
        constraint fk_following
            references users,
    userbeingfollowed varchar(255) not null
        constraint fk_followed
            references users,
    primary key (userfollowing, userbeingfollowed)
);

alter table follows
    owner to p32001_11;

