-- Ensure shops table has updated_at column and normalized commission rates
ALTER TABLE shops
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE shops
    ALTER COLUMN commission_rate SET DEFAULT 100.00;

UPDATE shops
SET commission_rate = ROUND(commission_rate * 100.0, 2)
WHERE commission_rate IS NOT NULL
  AND commission_rate BETWEEN 0 AND 1;

UPDATE shops
SET commission_rate = 0.00
WHERE commission_rate < 0;

UPDATE shops
SET commission_rate = 100.00
WHERE commission_rate IS NULL
   OR commission_rate > 100;

UPDATE shops
SET updated_at = CURRENT_TIMESTAMP
WHERE updated_at IS NULL;
