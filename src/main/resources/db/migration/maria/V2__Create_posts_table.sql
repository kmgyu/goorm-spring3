-- Create posts table
CREATE TABLE IF NOT EXISTS posts (
    seq BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_seq BIGINT NOT NULL,
    view_count BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_posts_user_seq (user_seq),
    INDEX idx_posts_created_at (created_at),
    INDEX idx_posts_title (title),

    CONSTRAINT fk_posts_user_seq
        FOREIGN KEY (user_seq)
        REFERENCES users(user_seq)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;