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
    works             text default 'works_fine',               -- can be broken, would_not_work, works_fine
    last_updated      timestamp without time zone
                           default (now() at time zone 'utc'), -- utc timestamp
    schedule          text,                                    -- example: '09:00-17:00'
    corrections_count int  default 0 not null,
    address           text
);

CREATE INDEX full_text_by_address_ru ON collection_point USING GIN (to_tsvector('russian', address));