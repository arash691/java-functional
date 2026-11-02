create table product (
    id    bigint generated always as identity primary key,
    sku   varchar(40)    not null unique,
    name  varchar(200)   not null,
    price numeric(12, 2) not null check (price >= 0)
);
