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
    id           serial primary key,
    gid          int unique not null,
    feature_name text,
    plastic      bool       not null,
    glass        bool       not null,
    paper        bool       not null,
    metal        bool       not null,
    tetra_pack   bool       not null,
    batteries    bool       not null,
    light_bulbs  bool       not null,
    clothes      bool       not null,
    appliances   boolean    not null,
    toxic        bool       not null,
    other        bool       not null,
    caps         bool       not null,
    tires        bool       not null,
    geom         geometry
);