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
    gid               int unique not null,
    feature_name      text       not null,
    plastic           bool       not null,
    glass             bool       not null,
    paper             bool       not null,
    metal             bool       not null,
    tetra_pack        bool       not null,
    batteries         bool       not null,
    light_bulbs       bool       not null,
    clothes           bool       not null,
    appliances        bool       not null,
    toxic             bool       not null,
    other             bool       not null,
    caps              bool       not null,
    tires             bool       not null,
    geom              geometry   not null,
    phone_number      text,
    web_site          text,
    works             text,                 -- can be broken, would_not_work, works_fine
    last_updated      timestamp without time zone
        default (now() at time zone 'utc'), -- utc timestamp
    schedule          text,                 -- example: '09:00-17:00'
    corrections_count int
);