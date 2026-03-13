-- Legacy schema alignment for existing non-empty databases that baseline at version 1.
-- Keep this migration idempotent so it is safe after B1 (empty DB) and after historical V1 executions.
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


