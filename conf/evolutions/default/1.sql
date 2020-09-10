# --- !Ups

CREATE TABLE users (
    id varchar(36) primary key,
    name text NOT NULL,
    age integer NOT NULL,
    sex text NOT NULL,
    enterprise_id varchar(36)
);

CREATE TABLE exps (
    id varchar(36) primary key,
    name text NOT NULL,
    location text NOT NULL,
    start_at date NOT NULL,
    end_at date,
    user_id
);

CREATE TABLE enterprises (
    id varchar(36) primary key,
    name text NOT NULL,
    location text NOT NULL
);


# --- !Downs

DROP TABLE users;
DROP TABLE exps;
DROP TABLE enterprises;