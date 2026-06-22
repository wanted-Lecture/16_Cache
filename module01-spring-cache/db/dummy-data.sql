-- k6 테스트에서 DB 조회 비용이 보이도록 충분한 더미 데이터를 넣는다.
-- 이 파일은 products 테이블에 8,000건의 상품 데이터를 생성한다.
-- 같은 id와 같은 검색 조건을 반복 호출했을 때 캐시 효과가 잘 보이도록 데이터 패턴을 의도적으로 만든다.

-- schema.sql에서 만든 menudb를 사용한다.
-- use 문을 먼저 실행해야 이후 delete, insert, select가 menudb의 products 테이블을 대상으로 동작한다.
use menudb;

-- MySQL 재귀 CTE는 기본 재귀 횟수 제한이 낮으므로 8,000행 생성을 위해 제한을 올린다.
-- seq CTE가 1부터 8,000까지 숫자를 만들 예정이므로 최소 8,000번 이상 재귀가 허용되어야 한다.
set session cte_max_recursion_depth = 10000;

-- 스크립트를 여러 번 실행해도 결과가 중복되지 않도록 기존 데이터를 비운다.
-- delete는 테이블 구조는 유지하고 행 데이터만 삭제한다.
# delete from products;

-- auto_increment 값을 다시 1부터 시작하게 맞춘다.
-- 이번 insert는 id를 직접 넣지만, 이후 수동 테스트에서 새 데이터가 예측 가능한 id로 생성되게 한다.
alter table products auto_increment = 1;

-- insert ... select 구조로 8,000건을 한 번에 생성한다.
-- values를 8,000줄 직접 쓰지 않고, select 결과를 products 테이블에 삽입한다.
insert into products (id, name, category, price, stock, popularity, updated_at)
-- recursive CTE로 1부터 8,000까지 숫자 테이블을 임시로 만든다.
-- 첫 select 1이 시작점이고, union all 아래 select n + 1이 다음 숫자를 계속 만든다.
-- where n < 8000 조건이 있어 n이 8,000이 되면 재귀 생성을 멈춘다.
with recursive seq(n) as (
    select 1
    union all
    select n + 1
    from seq
    where n < 8000
)
select
    -- CTE가 만든 숫자 n을 상품 id로 그대로 사용한다.
    n as id,

    -- 상품명에는 카테고리, 번호, 일부 popular 키워드를 섞는다.
    -- concat은 여러 문자열을 이어 붙인다.
    -- lower는 카테고리 문자열을 소문자로 바꿔 상품명 앞부분에 사용한다.
    -- lpad(n, 4, '0')은 1을 0001처럼 4자리 문자열로 맞춘다.
    -- mod(n, 10) = 0인 상품만 popular를 붙여 반복 검색 대상이 되게 한다.
    concat(
        lower(
            case mod(n, 6)
                when 0 then 'BOOK'
                when 1 then 'FOOD'
                when 2 then 'DIGITAL'
                when 3 then 'BEAUTY'
                when 4 then 'SPORTS'
                else 'LIVING'
            end
        ),
        ' lesson item ',
        lpad(n, 4, '0'),
        case when mod(n, 10) = 0 then ' popular' else '' end
    ) as name,

    -- mod(n, 6)으로 6개 카테고리에 상품을 고르게 분산한다.
    -- 나머지 값이 0~5로 반복되므로 BOOK, FOOD, DIGITAL, BEAUTY, SPORTS, LIVING이 순환 배치된다.
    case mod(n, 6)
        when 0 then 'BOOK'
        when 1 then 'FOOD'
        when 2 then 'DIGITAL'
        when 3 then 'BEAUTY'
        when 4 then 'SPORTS'
        else 'LIVING'
    end as category,

    -- 가격 조건 검색을 실습할 수 있도록 1,000원부터 90,900원까지 반복 패턴을 만든다.
    -- mod(n, 900)은 0~899를 반복하고, 여기에 100을 곱해 다양한 가격대를 만든다.
    1000 + mod(n, 900) * 100 as price,

    -- 재고 변경 API 실습을 위해 상품마다 다른 재고 값을 만든다.
    -- 20~319 사이의 값이 반복되어 stock 필드가 모두 같은 값이 되지 않는다.
    20 + mod(n, 300) as stock,

    -- 검색 결과 정렬이 매번 같도록 popularity 값을 결정적으로 계산한다.
    -- 1,000번 근처 상품의 인기도가 높게 나오도록 abs(1000 - n)을 사용한다.
    -- mod(n, 97)을 더해 같은 거리의 상품도 약간씩 다른 점수를 갖게 한다.
    10000 - abs(1000 - n) + mod(n, 97) as popularity,

    -- updated_at도 상품마다 조금씩 다르게 만든다.
    -- now(6)는 현재 시간을 마이크로초 정밀도로 만들고, date_sub은 일정 시간만큼 과거로 뺀다.
    -- mod(n, 1440)은 하루 1,440분 범위 안에서 갱신 시간을 분산한다.
    date_sub(now(6), interval mod(n, 1440) minute) as updated_at
from seq;

-- 마지막에 삽입 건수를 확인해 수업 시작 전에 데이터 준비가 끝났는지 검증한다.
-- product_count가 8000이면 더미 데이터가 정상적으로 들어간 것이다.
select count(*) as product_count from products;
