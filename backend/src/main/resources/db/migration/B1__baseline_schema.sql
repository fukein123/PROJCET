CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    gender VARCHAR(16),
    avatar VARCHAR(255),
    role VARCHAR(32) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    real_name VARCHAR(64),
    certified TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activity_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    address VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PUBLISHED',
    target_count INT NOT NULL DEFAULT 1,
    volunteer_quota INT NOT NULL DEFAULT 1,
    content VARCHAR(500),
    description TEXT,
    cover_image VARCHAR(255),
    creator_id BIGINT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activity_category (category_id),
    INDEX idx_activity_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @activity_cover_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'activity'
      AND COLUMN_NAME = 'cover_image'
);
SET @activity_cover_sql = IF(@activity_cover_exists = 0,
                             'ALTER TABLE activity ADD COLUMN cover_image VARCHAR(255) NULL AFTER description',
                             'SELECT 1');
PREPARE activity_cover_stmt FROM @activity_cover_sql;
EXECUTE activity_cover_stmt;
DEALLOCATE PREPARE activity_cover_stmt;

SET @activity_volunteer_quota_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'activity'
      AND COLUMN_NAME = 'volunteer_quota'
);
SET @activity_volunteer_quota_sql = IF(@activity_volunteer_quota_exists = 0,
                                       'ALTER TABLE activity ADD COLUMN volunteer_quota INT NOT NULL DEFAULT 1 AFTER target_count',
                                       'SELECT 1');
PREPARE activity_volunteer_quota_stmt FROM @activity_volunteer_quota_sql;
EXECUTE activity_volunteer_quota_stmt;
DEALLOCATE PREPARE activity_volunteer_quota_stmt;

SET @activity_content_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'activity'
      AND COLUMN_NAME = 'content'
);
SET @activity_content_sql = IF(@activity_content_exists = 0,
                               'ALTER TABLE activity ADD COLUMN content VARCHAR(500) NULL AFTER volunteer_quota',
                               'SELECT 1');
PREPARE activity_content_stmt FROM @activity_content_sql;
EXECUTE activity_content_stmt;
DEALLOCATE PREPARE activity_content_stmt;

SET @activity_latitude_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'activity'
      AND COLUMN_NAME = 'latitude'
);
SET @activity_latitude_sql = IF(@activity_latitude_exists = 1,
                                'ALTER TABLE activity DROP COLUMN latitude',
                                'SELECT 1');
PREPARE activity_latitude_stmt FROM @activity_latitude_sql;
EXECUTE activity_latitude_stmt;
DEALLOCATE PREPARE activity_latitude_stmt;

SET @activity_longitude_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'activity'
      AND COLUMN_NAME = 'longitude'
);
SET @activity_longitude_sql = IF(@activity_longitude_exists = 1,
                                 'ALTER TABLE activity DROP COLUMN longitude',
                                 'SELECT 1');
PREPARE activity_longitude_stmt FROM @activity_longitude_sql;
EXECUTE activity_longitude_stmt;
DEALLOCATE PREPARE activity_longitude_stmt;

CREATE TABLE IF NOT EXISTS activity_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    apply_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audit_time DATETIME,
    auditor_id BIGINT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_activity_user (activity_id, user_id),
    INDEX idx_application_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activity_check_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    sign_in_time DATETIME,
    sign_out_time DATETIME,
    sign_in_distance DECIMAL(10, 2),
    sign_out_distance DECIMAL(10, 2),
    status VARCHAR(32),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_check_record (activity_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS info_dynamic (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(255),
    type VARCHAR(64) NOT NULL DEFAULT 'NEWS',
    views INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    author_id BIGINT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @dynamic_image_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'info_dynamic'
      AND COLUMN_NAME = 'image_url'
);
SET @dynamic_image_sql = IF(@dynamic_image_exists = 0,
                            'ALTER TABLE info_dynamic ADD COLUMN image_url VARCHAR(255) NULL AFTER content',
                            'SELECT 1');
PREPARE dynamic_image_stmt FROM @dynamic_image_sql;
EXECUTE dynamic_image_stmt;
DEALLOCATE PREPARE dynamic_image_stmt;

CREATE TABLE IF NOT EXISTS forum_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS forum_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    views INT NOT NULL DEFAULT 0,
    audit_reason VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_post_status (status),
    INDEX idx_post_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comment_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type VARCHAR(32) NOT NULL,
    target_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    test_data_tag VARCHAR(64),
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_comment_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @comment_test_data_tag_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'comment_info'
      AND COLUMN_NAME = 'test_data_tag'
);
SET @comment_test_data_tag_sql = IF(@comment_test_data_tag_exists = 0,
                                    'ALTER TABLE comment_info ADD COLUMN test_data_tag VARCHAR(64) NULL AFTER content',
                                    'SELECT 1');
PREPARE comment_test_data_tag_stmt FROM @comment_test_data_tag_sql;
EXECUTE comment_test_data_tag_stmt;
DEALLOCATE PREPARE comment_test_data_tag_stmt;

CREATE TABLE IF NOT EXISTS banner_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    activity_id BIGINT,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notice_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    publish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exchange_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    points INT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS favorite_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    activity_title VARCHAR(255),
    activity_address VARCHAR(255),
    activity_start_time DATETIME,
    activity_end_time DATETIME,
    note VARCHAR(255),
    tag VARCHAR(64),
    priority INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_favorite_user_activity (user_id, activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @favorite_note_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorite_activity'
      AND COLUMN_NAME = 'note'
);
SET @favorite_note_sql = IF(@favorite_note_exists = 0,
                            'ALTER TABLE favorite_activity ADD COLUMN note VARCHAR(255) NULL AFTER activity_id',
                            'SELECT 1');
PREPARE favorite_note_stmt FROM @favorite_note_sql;
EXECUTE favorite_note_stmt;
DEALLOCATE PREPARE favorite_note_stmt;

SET @favorite_tag_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorite_activity'
      AND COLUMN_NAME = 'tag'
);
SET @favorite_tag_sql = IF(@favorite_tag_exists = 0,
                           'ALTER TABLE favorite_activity ADD COLUMN tag VARCHAR(64) NULL AFTER note',
                           'SELECT 1');
PREPARE favorite_tag_stmt FROM @favorite_tag_sql;
EXECUTE favorite_tag_stmt;
DEALLOCATE PREPARE favorite_tag_stmt;

SET @favorite_priority_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorite_activity'
      AND COLUMN_NAME = 'priority'
);
SET @favorite_priority_sql = IF(@favorite_priority_exists = 0,
                                'ALTER TABLE favorite_activity ADD COLUMN priority INT NOT NULL DEFAULT 0 AFTER tag',
                                'SELECT 1');
PREPARE favorite_priority_stmt FROM @favorite_priority_sql;
EXECUTE favorite_priority_stmt;
DEALLOCATE PREPARE favorite_priority_stmt;

SET @favorite_activity_title_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorite_activity'
      AND COLUMN_NAME = 'activity_title'
);
SET @favorite_activity_title_sql = IF(@favorite_activity_title_exists = 0,
                                      'ALTER TABLE favorite_activity ADD COLUMN activity_title VARCHAR(255) NULL AFTER activity_id',
                                      'SELECT 1');
PREPARE favorite_activity_title_stmt FROM @favorite_activity_title_sql;
EXECUTE favorite_activity_title_stmt;
DEALLOCATE PREPARE favorite_activity_title_stmt;

SET @favorite_activity_address_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorite_activity'
      AND COLUMN_NAME = 'activity_address'
);
SET @favorite_activity_address_sql = IF(@favorite_activity_address_exists = 0,
                                        'ALTER TABLE favorite_activity ADD COLUMN activity_address VARCHAR(255) NULL AFTER activity_title',
                                        'SELECT 1');
PREPARE favorite_activity_address_stmt FROM @favorite_activity_address_sql;
EXECUTE favorite_activity_address_stmt;
DEALLOCATE PREPARE favorite_activity_address_stmt;

SET @favorite_activity_start_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorite_activity'
      AND COLUMN_NAME = 'activity_start_time'
);
SET @favorite_activity_start_time_sql = IF(@favorite_activity_start_time_exists = 0,
                                           'ALTER TABLE favorite_activity ADD COLUMN activity_start_time DATETIME NULL AFTER activity_address',
                                           'SELECT 1');
PREPARE favorite_activity_start_time_stmt FROM @favorite_activity_start_time_sql;
EXECUTE favorite_activity_start_time_stmt;
DEALLOCATE PREPARE favorite_activity_start_time_stmt;

SET @favorite_activity_end_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'favorite_activity'
      AND COLUMN_NAME = 'activity_end_time'
);
SET @favorite_activity_end_time_sql = IF(@favorite_activity_end_time_exists = 0,
                                         'ALTER TABLE favorite_activity ADD COLUMN activity_end_time DATETIME NULL AFTER activity_start_time',
                                         'SELECT 1');
PREPARE favorite_activity_end_time_stmt FROM @favorite_activity_end_time_sql;
EXECUTE favorite_activity_end_time_stmt;
DEALLOCATE PREPARE favorite_activity_end_time_stmt;

UPDATE favorite_activity fa
LEFT JOIN activity a ON a.id = fa.activity_id
SET fa.activity_title = COALESCE(fa.activity_title, a.title),
    fa.activity_address = COALESCE(fa.activity_address, a.address),
    fa.activity_start_time = COALESCE(fa.activity_start_time, a.start_time),
    fa.activity_end_time = COALESCE(fa.activity_end_time, a.end_time)
WHERE a.id IS NOT NULL
  AND (
      fa.activity_title IS NULL
      OR fa.activity_address IS NULL
      OR fa.activity_start_time IS NULL
      OR fa.activity_end_time IS NULL
  );

CREATE TABLE IF NOT EXISTS volunteer_weekly_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    week_start DATE NOT NULL,
    user_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    real_name VARCHAR(64),
    completed_count INT NOT NULL DEFAULT 0,
    sign_in_count INT NOT NULL DEFAULT 0,
    sign_out_count INT NOT NULL DEFAULT 0,
    service_minutes INT NOT NULL DEFAULT 0,
    rank_no INT NOT NULL DEFAULT 0,
    generated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_week_user (week_start, user_id),
    INDEX idx_week_rank (week_start, rank_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
