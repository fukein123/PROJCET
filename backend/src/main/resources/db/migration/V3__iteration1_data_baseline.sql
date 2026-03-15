-- Iteration 1 data baseline: fill key business fields needed by activity, content, and volunteer flows.

SET @activity_point_reward_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'activity'
      AND COLUMN_NAME = 'point_reward'
);
SET @activity_point_reward_sql = IF(@activity_point_reward_exists = 0,
                                    'ALTER TABLE activity ADD COLUMN point_reward INT NOT NULL DEFAULT 0 AFTER volunteer_quota',
                                    'SELECT 1');
PREPARE activity_point_reward_stmt FROM @activity_point_reward_sql;
EXECUTE activity_point_reward_stmt;
DEALLOCATE PREPARE activity_point_reward_stmt;

UPDATE activity
SET point_reward = 0
WHERE point_reward IS NULL;

SET @activity_application_reason_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'activity_application'
      AND COLUMN_NAME = 'apply_reason'
);
SET @activity_application_reason_sql = IF(@activity_application_reason_exists = 0,
                                          'ALTER TABLE activity_application ADD COLUMN apply_reason VARCHAR(500) NULL AFTER user_id',
                                          'SELECT 1');
PREPARE activity_application_reason_stmt FROM @activity_application_reason_sql;
EXECUTE activity_application_reason_stmt;
DEALLOCATE PREPARE activity_application_reason_stmt;

SET @dynamic_source_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'info_dynamic'
      AND COLUMN_NAME = 'source'
);
SET @dynamic_source_sql = IF(@dynamic_source_exists = 0,
                             'ALTER TABLE info_dynamic ADD COLUMN source VARCHAR(128) NOT NULL DEFAULT ''平台发布'' AFTER title',
                             'SELECT 1');
PREPARE dynamic_source_stmt FROM @dynamic_source_sql;
EXECUTE dynamic_source_stmt;
DEALLOCATE PREPARE dynamic_source_stmt;

UPDATE info_dynamic
SET source = '平台发布'
WHERE source IS NULL OR TRIM(source) = '';

SET @forum_post_cover_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'forum_post'
      AND COLUMN_NAME = 'cover_image'
);
SET @forum_post_cover_sql = IF(@forum_post_cover_exists = 0,
                               'ALTER TABLE forum_post ADD COLUMN cover_image VARCHAR(255) NULL AFTER title',
                               'SELECT 1');
PREPARE forum_post_cover_stmt FROM @forum_post_cover_sql;
EXECUTE forum_post_cover_stmt;
DEALLOCATE PREPARE forum_post_cover_stmt;

SET @forum_post_summary_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'forum_post'
      AND COLUMN_NAME = 'summary'
);
SET @forum_post_summary_sql = IF(@forum_post_summary_exists = 0,
                                 'ALTER TABLE forum_post ADD COLUMN summary VARCHAR(500) NULL AFTER cover_image',
                                 'SELECT 1');
PREPARE forum_post_summary_stmt FROM @forum_post_summary_sql;
EXECUTE forum_post_summary_stmt;
DEALLOCATE PREPARE forum_post_summary_stmt;

UPDATE forum_post
SET summary = LEFT(content, 120)
WHERE (summary IS NULL OR TRIM(summary) = '')
  AND content IS NOT NULL
  AND TRIM(content) <> '';

SET @user_points_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_user'
      AND COLUMN_NAME = 'points'
);
SET @user_points_sql = IF(@user_points_exists = 0,
                          'ALTER TABLE sys_user ADD COLUMN points INT NOT NULL DEFAULT 0 AFTER role',
                          'SELECT 1');
PREPARE user_points_stmt FROM @user_points_sql;
EXECUTE user_points_stmt;
DEALLOCATE PREPARE user_points_stmt;

UPDATE sys_user
SET points = 0
WHERE points IS NULL;
