-- Iteration 1 mall and exchange-order baseline.

CREATE TABLE IF NOT EXISTS mall_product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    points_cost INT NOT NULL DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @mall_product_status_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'mall_product'
      AND INDEX_NAME = 'idx_mall_product_status'
);
SET @mall_product_status_idx_sql = IF(
    @mall_product_status_idx_exists = 0,
    'CREATE INDEX idx_mall_product_status ON mall_product (status, create_time)',
    'SELECT 1'
);
PREPARE mall_product_status_idx_stmt FROM @mall_product_status_idx_sql;
EXECUTE mall_product_status_idx_stmt;
DEALLOCATE PREPARE mall_product_status_idx_stmt;

CREATE TABLE IF NOT EXISTS points_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    change_no VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    change_direction VARCHAR(16) NOT NULL,
    delta_points INT NOT NULL DEFAULT 0,
    source_type VARCHAR(32) NOT NULL,
    source_id BIGINT,
    reference_no VARCHAR(32),
    note VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @points_change_log_no_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'points_change_log'
      AND INDEX_NAME = 'uk_points_change_log_no'
);
SET @points_change_log_no_idx_sql = IF(
    @points_change_log_no_idx_exists = 0,
    'ALTER TABLE points_change_log ADD CONSTRAINT uk_points_change_log_no UNIQUE (change_no)',
    'SELECT 1'
);
PREPARE points_change_log_no_idx_stmt FROM @points_change_log_no_idx_sql;
EXECUTE points_change_log_no_idx_stmt;
DEALLOCATE PREPARE points_change_log_no_idx_stmt;

SET @points_change_log_user_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'points_change_log'
      AND INDEX_NAME = 'idx_points_change_log_user'
);
SET @points_change_log_user_idx_sql = IF(
    @points_change_log_user_idx_exists = 0,
    'CREATE INDEX idx_points_change_log_user ON points_change_log (user_id, create_time)',
    'SELECT 1'
);
PREPARE points_change_log_user_idx_stmt FROM @points_change_log_user_idx_sql;
EXECUTE points_change_log_user_idx_stmt;
DEALLOCATE PREPARE points_change_log_user_idx_stmt;

SET @exchange_order_order_no_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'order_no'
);
SET @exchange_order_order_no_sql = IF(
    @exchange_order_order_no_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN order_no VARCHAR(32) NULL AFTER id',
    'SELECT 1'
);
PREPARE exchange_order_order_no_stmt FROM @exchange_order_order_no_sql;
EXECUTE exchange_order_order_no_stmt;
DEALLOCATE PREPARE exchange_order_order_no_stmt;

SET @exchange_order_product_id_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'product_id'
);
SET @exchange_order_product_id_sql = IF(
    @exchange_order_product_id_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN product_id BIGINT NULL AFTER user_id',
    'SELECT 1'
);
PREPARE exchange_order_product_id_stmt FROM @exchange_order_product_id_sql;
EXECUTE exchange_order_product_id_stmt;
DEALLOCATE PREPARE exchange_order_product_id_stmt;

SET @exchange_order_product_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'product_name'
);
SET @exchange_order_product_name_sql = IF(
    @exchange_order_product_name_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN product_name VARCHAR(128) NULL AFTER product_id',
    'SELECT 1'
);
PREPARE exchange_order_product_name_stmt FROM @exchange_order_product_name_sql;
EXECUTE exchange_order_product_name_stmt;
DEALLOCATE PREPARE exchange_order_product_name_stmt;

SET @exchange_order_product_image_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'product_image'
);
SET @exchange_order_product_image_sql = IF(
    @exchange_order_product_image_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN product_image VARCHAR(255) NULL AFTER product_name',
    'SELECT 1'
);
PREPARE exchange_order_product_image_stmt FROM @exchange_order_product_image_sql;
EXECUTE exchange_order_product_image_stmt;
DEALLOCATE PREPARE exchange_order_product_image_stmt;

SET @exchange_order_product_summary_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'product_summary'
);
SET @exchange_order_product_summary_sql = IF(
    @exchange_order_product_summary_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN product_summary VARCHAR(500) NULL AFTER product_image',
    'SELECT 1'
);
PREPARE exchange_order_product_summary_stmt FROM @exchange_order_product_summary_sql;
EXECUTE exchange_order_product_summary_stmt;
DEALLOCATE PREPARE exchange_order_product_summary_stmt;

SET @exchange_order_user_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'user_name'
);
SET @exchange_order_user_name_sql = IF(
    @exchange_order_user_name_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN user_name VARCHAR(64) NULL AFTER user_id',
    'SELECT 1'
);
PREPARE exchange_order_user_name_stmt FROM @exchange_order_user_name_sql;
EXECUTE exchange_order_user_name_stmt;
DEALLOCATE PREPARE exchange_order_user_name_stmt;

SET @exchange_order_real_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'real_name'
);
SET @exchange_order_real_name_sql = IF(
    @exchange_order_real_name_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN real_name VARCHAR(64) NULL AFTER user_name',
    'SELECT 1'
);
PREPARE exchange_order_real_name_stmt FROM @exchange_order_real_name_sql;
EXECUTE exchange_order_real_name_stmt;
DEALLOCATE PREPARE exchange_order_real_name_stmt;

SET @exchange_order_quantity_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'quantity'
);
SET @exchange_order_quantity_sql = IF(
    @exchange_order_quantity_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN quantity INT NOT NULL DEFAULT 1 AFTER real_name',
    'SELECT 1'
);
PREPARE exchange_order_quantity_stmt FROM @exchange_order_quantity_sql;
EXECUTE exchange_order_quantity_stmt;
DEALLOCATE PREPARE exchange_order_quantity_stmt;

SET @exchange_order_points_per_item_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'points_per_item'
);
SET @exchange_order_points_per_item_sql = IF(
    @exchange_order_points_per_item_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN points_per_item INT NOT NULL DEFAULT 0 AFTER quantity',
    'SELECT 1'
);
PREPARE exchange_order_points_per_item_stmt FROM @exchange_order_points_per_item_sql;
EXECUTE exchange_order_points_per_item_stmt;
DEALLOCATE PREPARE exchange_order_points_per_item_stmt;

SET @exchange_order_total_points_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'total_points'
);
SET @exchange_order_total_points_sql = IF(
    @exchange_order_total_points_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN total_points INT NOT NULL DEFAULT 0 AFTER points_per_item',
    'SELECT 1'
);
PREPARE exchange_order_total_points_stmt FROM @exchange_order_total_points_sql;
EXECUTE exchange_order_total_points_stmt;
DEALLOCATE PREPARE exchange_order_total_points_stmt;

SET @exchange_order_receiver_name_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'receiver_name'
);
SET @exchange_order_receiver_name_sql = IF(
    @exchange_order_receiver_name_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN receiver_name VARCHAR(64) NULL AFTER total_points',
    'SELECT 1'
);
PREPARE exchange_order_receiver_name_stmt FROM @exchange_order_receiver_name_sql;
EXECUTE exchange_order_receiver_name_stmt;
DEALLOCATE PREPARE exchange_order_receiver_name_stmt;

SET @exchange_order_receiver_phone_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'receiver_phone'
);
SET @exchange_order_receiver_phone_sql = IF(
    @exchange_order_receiver_phone_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN receiver_phone VARCHAR(32) NULL AFTER receiver_name',
    'SELECT 1'
);
PREPARE exchange_order_receiver_phone_stmt FROM @exchange_order_receiver_phone_sql;
EXECUTE exchange_order_receiver_phone_stmt;
DEALLOCATE PREPARE exchange_order_receiver_phone_stmt;

SET @exchange_order_receiver_address_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'receiver_address'
);
SET @exchange_order_receiver_address_sql = IF(
    @exchange_order_receiver_address_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN receiver_address VARCHAR(255) NULL AFTER receiver_phone',
    'SELECT 1'
);
PREPARE exchange_order_receiver_address_stmt FROM @exchange_order_receiver_address_sql;
EXECUTE exchange_order_receiver_address_stmt;
DEALLOCATE PREPARE exchange_order_receiver_address_stmt;

SET @exchange_order_request_key_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'request_key'
);
SET @exchange_order_request_key_sql = IF(
    @exchange_order_request_key_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN request_key VARCHAR(64) NULL AFTER receiver_address',
    'SELECT 1'
);
PREPARE exchange_order_request_key_stmt FROM @exchange_order_request_key_sql;
EXECUTE exchange_order_request_key_stmt;
DEALLOCATE PREPARE exchange_order_request_key_stmt;

SET @exchange_order_status_reason_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'status_reason'
);
SET @exchange_order_status_reason_sql = IF(
    @exchange_order_status_reason_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN status_reason VARCHAR(255) NULL AFTER request_key',
    'SELECT 1'
);
PREPARE exchange_order_status_reason_stmt FROM @exchange_order_status_reason_sql;
EXECUTE exchange_order_status_reason_stmt;
DEALLOCATE PREPARE exchange_order_status_reason_stmt;

SET @exchange_order_shipped_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'shipped_time'
);
SET @exchange_order_shipped_time_sql = IF(
    @exchange_order_shipped_time_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN shipped_time DATETIME NULL AFTER status_reason',
    'SELECT 1'
);
PREPARE exchange_order_shipped_time_stmt FROM @exchange_order_shipped_time_sql;
EXECUTE exchange_order_shipped_time_stmt;
DEALLOCATE PREPARE exchange_order_shipped_time_stmt;

SET @exchange_order_received_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'received_time'
);
SET @exchange_order_received_time_sql = IF(
    @exchange_order_received_time_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN received_time DATETIME NULL AFTER shipped_time',
    'SELECT 1'
);
PREPARE exchange_order_received_time_stmt FROM @exchange_order_received_time_sql;
EXECUTE exchange_order_received_time_stmt;
DEALLOCATE PREPARE exchange_order_received_time_stmt;

SET @exchange_order_cancelled_time_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND COLUMN_NAME = 'cancelled_time'
);
SET @exchange_order_cancelled_time_sql = IF(
    @exchange_order_cancelled_time_exists = 0,
    'ALTER TABLE exchange_order ADD COLUMN cancelled_time DATETIME NULL AFTER received_time',
    'SELECT 1'
);
PREPARE exchange_order_cancelled_time_stmt FROM @exchange_order_cancelled_time_sql;
EXECUTE exchange_order_cancelled_time_stmt;
DEALLOCATE PREPARE exchange_order_cancelled_time_stmt;

SET @exchange_order_no_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND INDEX_NAME = 'uk_exchange_order_no'
);
SET @exchange_order_no_idx_sql = IF(
    @exchange_order_no_idx_exists = 0,
    'ALTER TABLE exchange_order ADD CONSTRAINT uk_exchange_order_no UNIQUE (order_no)',
    'SELECT 1'
);
PREPARE exchange_order_no_idx_stmt FROM @exchange_order_no_idx_sql;
EXECUTE exchange_order_no_idx_stmt;
DEALLOCATE PREPARE exchange_order_no_idx_stmt;

SET @exchange_order_request_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND INDEX_NAME = 'uk_exchange_order_user_request'
);
SET @exchange_order_request_idx_sql = IF(
    @exchange_order_request_idx_exists = 0,
    'ALTER TABLE exchange_order ADD CONSTRAINT uk_exchange_order_user_request UNIQUE (user_id, request_key)',
    'SELECT 1'
);
PREPARE exchange_order_request_idx_stmt FROM @exchange_order_request_idx_sql;
EXECUTE exchange_order_request_idx_stmt;
DEALLOCATE PREPARE exchange_order_request_idx_stmt;

SET @exchange_order_status_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND INDEX_NAME = 'idx_exchange_order_status'
);
SET @exchange_order_status_idx_sql = IF(
    @exchange_order_status_idx_exists = 0,
    'CREATE INDEX idx_exchange_order_status ON exchange_order (status, create_time)',
    'SELECT 1'
);
PREPARE exchange_order_status_idx_stmt FROM @exchange_order_status_idx_sql;
EXECUTE exchange_order_status_idx_stmt;
DEALLOCATE PREPARE exchange_order_status_idx_stmt;

SET @exchange_order_user_idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'exchange_order'
      AND INDEX_NAME = 'idx_exchange_order_user'
);
SET @exchange_order_user_idx_sql = IF(
    @exchange_order_user_idx_exists = 0,
    'CREATE INDEX idx_exchange_order_user ON exchange_order (user_id, create_time)',
    'SELECT 1'
);
PREPARE exchange_order_user_idx_stmt FROM @exchange_order_user_idx_sql;
EXECUTE exchange_order_user_idx_stmt;
DEALLOCATE PREPARE exchange_order_user_idx_stmt;

UPDATE exchange_order
SET quantity = 1
WHERE quantity IS NULL OR quantity <= 0;

UPDATE exchange_order
SET product_name = COALESCE(NULLIF(product_name, ''), item_name),
    points_per_item = CASE
        WHEN points_per_item IS NULL OR points_per_item < 0 THEN 0
        WHEN points_per_item = 0 AND points IS NOT NULL THEN points
        ELSE points_per_item
    END;

UPDATE exchange_order
SET total_points = quantity * points_per_item
WHERE total_points IS NULL OR total_points < 0 OR total_points = 0;

UPDATE exchange_order
SET order_no = CONCAT('ORDHIS', LPAD(id, 10, '0'))
WHERE order_no IS NULL OR TRIM(order_no) = '';

UPDATE exchange_order
SET receiver_name = '历史订单'
WHERE receiver_name IS NULL OR TRIM(receiver_name) = '';

UPDATE exchange_order
SET receiver_address = '历史地址待补全'
WHERE receiver_address IS NULL OR TRIM(receiver_address) = '';

UPDATE exchange_order
SET status = 'CREATED'
WHERE status IS NULL OR TRIM(status) = '' OR status = 'PENDING';
