create table cart (
    id          bigint generated always as identity primary key,
    customer_id varchar(64) not null unique
);

create table cart_item (
    id         bigint generated always as identity primary key,
    cart_id    bigint not null references cart (id) on delete cascade,
    product_id bigint not null references product (id),
    quantity   int    not null check (quantity > 0),
    unique (cart_id, product_id)
);
