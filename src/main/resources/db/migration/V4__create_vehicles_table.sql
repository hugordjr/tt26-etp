CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_plate VARCHAR(15) NOT NULL,
    sector_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    entry_time DATETIME(6) NOT NULL,
    parked_time DATETIME(6),
    exit_time DATETIME(6),
    adjusted_price DECIMAL(12, 2) NOT NULL,
    status VARCHAR(10) NOT NULL,
    FOREIGN KEY (sector_id) REFERENCES sectors(id) ON DELETE CASCADE,
    FOREIGN KEY (spot_id) REFERENCES spots(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_vehicles_license_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_sector_id ON vehicles(sector_id);
CREATE INDEX idx_vehicles_spot_id ON vehicles(spot_id);
CREATE INDEX idx_vehicles_status ON vehicles(status);
