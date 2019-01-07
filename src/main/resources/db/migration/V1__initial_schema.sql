create table "task" (
"id" UUID PRIMARY KEY,
"title" VARCHAR NOT NULL,
"description" VARCHAR,
"completed" BOOLEAN NOT NULL,
"created" TIMESTAMP NOT NULL,
"due" TIMESTAMP);