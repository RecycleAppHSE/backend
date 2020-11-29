create database rcapp;

create table rc_user
(
    id        serial primary key,
    name      text,
    photo_url text
);

create extension postgis;

create table collection_point
(
    id                serial primary key,
    -- recycle map field
    gid               int unique     not null,
    feature_name      text           not null,
    plastic           bool           not null,
    glass             bool           not null,
    paper             bool           not null,
    metal             bool           not null,
    tetra_pack        bool           not null,
    batteries         bool           not null,
    light_bulbs       bool           not null,
    clothes           bool           not null,
    appliances        bool           not null,
    toxic             bool           not null,
    other             bool           not null,
    caps              bool           not null,
    tires             bool           not null,
    geom              geometry       not null,
    -- additional fields
    phone_number      text,
    web_site          text,
    works             text default 'works_fine',               -- может быть broken, would_not_work, works_fine
    last_updated      timestamp without time zone
                           default (now() at time zone 'utc'), -- utc timestamp
    schedule          text,                                    -- example: '09:00-17:00'
    corrections_count int  default 0 not null,
    address           text
);

CREATE INDEX full_text_by_address ON collection_point USING GIN (to_tsvector('english', address));

CREATE TYPE collection_point_field_for_change AS ENUM (
    'recycle', -- Поле состоящее из столбцов plastic, glass, metal, ... tires.
    'works'    -- Поле works в таблице collection_point
);

CREATE TYPE correction_status AS ENUM (
    'applied',    -- изменение не было применено
    'rejected',   -- изменение было отклонено
    'in-progress' -- коррекция в находится в процессе голосования
);

create table correction
(
    rc_user_id          integer                           not null,
    collection_point_id integer                           not null,
    field               collection_point_field_for_change not null, -- поле в структуре collection_point.
    change_from         text                              not null, -- Пример: ["paper", "toxic"]
    change_to           text                              not null, -- Пример: ["light_bulbs", "glass"]
    status              correction_status                 not null, -- Статус коррекции
    submit_time         timestamp without time zone
        default (now() at time zone 'utc'),                         -- utc timestamp
    constraint fk_correction_rc_user_id foreign key (rc_user_id) references rc_user (id),
    constraint fk_correction_collection_point_id foreign key (collection_point_id) references collection_point (id)
);

create index correction_by_user_id on correction(rc_user_id);
create index correction_by_collection_point_id on correction(rc_user_id);
create index correction_ftype_and_collection_point_id on correction(collection_point_id, field);
