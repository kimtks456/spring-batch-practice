CREATE TABLE orders (
    id            BIGSERIAL      PRIMARY KEY,
    customer_name VARCHAR(100)   NOT NULL,
    amount        NUMERIC(12, 2) NOT NULL,
    status        VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    updated_at    TIMESTAMP
);

-- 10000건 랜덤 데이터 (amount 범위: 1~100000, amount > 10000 조건 대상은 약 9000건)
INSERT INTO orders (customer_name, amount, status)
SELECT
    'Customer_' || i,
    (random() * 99999 + 1)::numeric(12, 2),
    'PENDING'
FROM generate_series(1, 10000) AS i;
