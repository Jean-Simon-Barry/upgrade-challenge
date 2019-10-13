create table camping_reservation
(
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(255) not null,
    user_email VARCHAR(255) not null,
    reservation_dates daterange not null,
    EXCLUDE USING gist (reservation_dates WITH &&)
);
CREATE INDEX reservation_idx ON camping_reservation USING gist (reservation_dates);
