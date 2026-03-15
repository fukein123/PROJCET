CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,
    operator_username VARCHAR(64),
    action_type VARCHAR(64) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    target_id BIGINT,
    target_name VARCHAR(255),
    result VARCHAR(32) NOT NULL DEFAULT 'SUCCESS',
    detail VARCHAR(255),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_admin_operation_log_operator (operator_id),
    INDEX idx_admin_operation_log_action (action_type),
    INDEX idx_admin_operation_log_target (target_type, target_id),
    INDEX idx_admin_operation_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
