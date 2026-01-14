CREATE TABLE IF NOT EXISTS spots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sector_id BIGINT NOT NULL,
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    occupied BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (sector_id) REFERENCES sectors(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_spots_sector_id ON spots(sector_id);
CREATE INDEX idx_spots_occupied ON spots(occupied);
