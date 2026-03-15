-- Iteration 1 database cleanup:
-- 1) normalize garbled historical text values introduced during earlier local migrations
-- 2) drop obsolete exchange_order legacy columns after V4 backfill
-- 3) remove redundant legacy cvs_* tables only when they are confirmed empty or fully migrated

UPDATE info_dynamic
SET source = '平台发布'
WHERE source = '骞冲彴鍙戝竷';

UPDATE exchange_order
SET receiver_name = '历史订单'
WHERE receiver_name = '鍘嗗彶璁㈠崟';

UPDATE exchange_order
SET receiver_address = '历史地址待补全'
WHERE receiver_address = '鍘嗗彶鍦板潃寰呰ˉ鍏?';

SET @exchange_order_item_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'item_name'
);
SET @exchange_order_product_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'product_name'
);
SET @exchange_order_drop_item_name_sql = IF(
    @exchange_order_item_name_exists = 1 AND @exchange_order_product_name_exists = 1,
    'ALTER TABLE exchange_order DROP COLUMN item_name',
    'SELECT 1'
);
PREPARE exchange_order_drop_item_name_stmt FROM @exchange_order_drop_item_name_sql;
EXECUTE exchange_order_drop_item_name_stmt;
DEALLOCATE PREPARE exchange_order_drop_item_name_stmt;

SET @exchange_order_points_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'points'
);
SET @exchange_order_points_per_item_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'points_per_item'
);
SET @exchange_order_total_points_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'total_points'
);
SET @exchange_order_drop_points_sql = IF(
    @exchange_order_points_exists = 1
    AND @exchange_order_points_per_item_exists = 1
    AND @exchange_order_total_points_exists = 1,
    'ALTER TABLE exchange_order DROP COLUMN points',
    'SELECT 1'
);
PREPARE exchange_order_drop_points_stmt FROM @exchange_order_drop_points_sql;
EXECUTE exchange_order_drop_points_stmt;
DEALLOCATE PREPARE exchange_order_drop_points_stmt;

DROP TABLE IF EXISTS cvs_activity;
DROP TABLE IF EXISTS cvs_activity_apply;
DROP TABLE IF EXISTS cvs_activity_category;
DROP TABLE IF EXISTS cvs_activity_clock;
DROP TABLE IF EXISTS cvs_banner;
DROP TABLE IF EXISTS cvs_comment;
DROP TABLE IF EXISTS cvs_exchange_order;
DROP TABLE IF EXISTS cvs_favorite;
DROP TABLE IF EXISTS cvs_forum_category;
DROP TABLE IF EXISTS cvs_forum_post;
DROP TABLE IF EXISTS cvs_news;
DROP TABLE IF EXISTS cvs_notice;
DROP TABLE IF EXISTS cvs_points_log;
DROP TABLE IF EXISTS cvs_user;
