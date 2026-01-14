CREATE TABLE IF NOT EXISTS sectors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    base_price DECIMAL(12, 2) NOT NULL,
    max_capacity INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sectors_code ON sectors(code);
