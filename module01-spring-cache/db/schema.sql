-- Java 코드를 작성하기 전에 수업용 데이터베이스와 테이블을 만든다.
-- 애플리케이션은 이 테이블을 직접 생성하지 않고, ddl-auto=validate로 구조만 검증한다.

create database if not exists menudb
    default character set utf8mb4
    collate utf8mb4_unicode_ci;

create user if not exists 'wanted'@'localhost' identified by 'wanted';
grant all privileges on menudb.* to 'wanted'@'localhost';

use menudb;

drop table if exists products;

create table products (
    id bigint not null auto_increment,
    name varchar(120) not null,
    category varchar(30) not null,
    price int not null,
    stock int not null,
    popularity int not null,
    updated_at datetime(6) not null,
    primary key (id),
    index idx_products_category_popularity (category, popularity desc),
    index idx_products_price (price),
    index idx_products_updated_at (updated_at)
) engine = InnoDB;
