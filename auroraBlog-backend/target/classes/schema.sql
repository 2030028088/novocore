CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    title_zh VARCHAR(255),
    title_en VARCHAR(255),
    category VARCHAR(50) DEFAULT 'WEB',
    tags TEXT,
    year VARCHAR(10),
    image VARCHAR(255),
    detail_image VARCHAR(255),
    description TEXT,
    description_zh TEXT,
    description_en TEXT,
    live_url VARCHAR(255),
    github_url VARCHAR(255),
    role VARCHAR(100),
    client VARCHAR(100),
    published BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_projects_category (category),
    INDEX idx_projects_published (published),
    INDEX idx_projects_created_at (created_at),
    FULLTEXT INDEX ft_projects_title (title, title_zh, title_en)
);

CREATE TABLE IF NOT EXISTS articles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    title_zh VARCHAR(255),
    title_en VARCHAR(255),
    summary TEXT,
    summary_zh TEXT,
    summary_en TEXT,
    content LONGTEXT,
    content_zh LONGTEXT,
    content_en LONGTEXT,
    category VARCHAR(50) DEFAULT 'DEVELOPMENT',
    tags TEXT,
    author VARCHAR(100) DEFAULT 'SYS_ADMIN',
    cover_image VARCHAR(255),
    read_time INT DEFAULT 5,
    favorites INT DEFAULT 0,
    published BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_articles_category (category),
    INDEX idx_articles_published (published),
    INDEX idx_articles_created_at (created_at),
    FULLTEXT INDEX ft_articles_search (title, title_zh, summary, summary_zh)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    likes INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comments_article_id (article_id),
    INDEX idx_comments_parent_id (parent_id),
    INDEX idx_comments_user_id (user_id),
    CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS downloads (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    description TEXT,
    file_size BIGINT,
    file_type VARCHAR(100),
    file_path VARCHAR(500) NOT NULL,
    published BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_downloads_published (published),
    INDEX idx_downloads_created_at (created_at),
    INDEX idx_downloads_file_type (file_type)
);

INSERT IGNORE INTO users (username, email, password, role, email_verified) VALUES 
('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'ADMIN', TRUE);