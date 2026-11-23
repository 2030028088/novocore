CREATE DATABASE IF NOT EXISTS aurora_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE aurora_blog;

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    title_zh VARCHAR(255),
    title_en VARCHAR(255),
    category VARCHAR(50),
    tags JSON,
    year VARCHAR(4),
    image TEXT,
    detail_image TEXT,
    description TEXT,
    description_zh TEXT,
    description_en TEXT,
    live_url TEXT,
    github_url TEXT,
    role VARCHAR(255),
    client VARCHAR(255),
    published BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_published (published),
    INDEX idx_category (category),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO projects (title, title_zh, category, tags, year, description, description_zh, published) VALUES
('E-Commerce Platform', 'E-commerce Platform', 'WEB', '["Vue.js", "Node.js", "MongoDB"]', '2024', 'A modern e-commerce platform', 'Modern e-commerce platform', true),
('Mobile Banking App', 'Mobile Banking App', 'APP', '["React Native", "TypeScript"]', '2024', 'Secure mobile banking application', 'Secure mobile banking', true),
('AI Content Generator', 'AI Content Generator', 'WEB', '["Python", "TensorFlow", "React"]', '2023', 'AI-powered content generation', 'AI content generation', true);
