CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(255),
    recipient VARCHAR(255),
    subject VARCHAR(255),
    content TEXT,
    created_at DATETIME
);
