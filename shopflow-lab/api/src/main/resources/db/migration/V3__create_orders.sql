-- "order" is a reserved word in SQL, so the table is called customer_order.
create table customer_order (
    id          bigint generated always as identity primary key,
    customer_id varchar(64)  not null,
    status      varchar(20)  not null,
    created_at  timestamptz  not null
);

create table order_item (
    id         bigint generated always as identity primary key,
    order_id   bigint         not null references customer_order (id),
    product_id bigint         not null references product (id),
    quantity   int            not null check (quantity > 0),
    unit_price numeric(12, 2) not null
);

create index ix_order_item_order on order_item (order_id);
