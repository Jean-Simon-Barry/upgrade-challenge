create table camping_user
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) not null,
    last_name VARCHAR(255) not null,
    email VARCHAR(255) not null,
    UNIQUE (email)
);

create table camping_reservation
(
    id BIGSERIAL PRIMARY KEY,
    camping_user_id bigint references camping_user(id),
    reservation_dates daterange not null,
    EXCLUDE USING gist (reservation_dates WITH &&)
);

CREATE INDEX reservation_idx ON camping_reservation USING gist (reservation_dates);
