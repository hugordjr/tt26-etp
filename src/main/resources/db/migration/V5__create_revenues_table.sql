CREATE TABLE IF NOT EXISTS revenues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sector_id BIGINT NOT NULL,
    date DATE NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    currency VARCHAR(5) NOT NULL,
    timestamp DATETIME(6) NOT NULL,
    FOREIGN KEY (sector_id) REFERENCES sectors(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_revenues_sector_id ON revenues(sector_id);
CREATE INDEX idx_revenues_date ON revenues(date);
CREATE INDEX idx_revenues_sector_date ON revenues(sector_id, date);
