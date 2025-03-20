create table email
(
    username varchar(255) not null
        references users,
    email    varchar(255) not null
        constraint chk_email_format
            check (("position"((email)::text, '@'::text) > 1) AND
                   ("position"((email)::text, '.'::text) > ("position"((email)::text, '@'::text) + 1))),
    primary key (username, email)
);

alter table email
    owner to p32001_11;
