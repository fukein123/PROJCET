CREATE TABLE IF NOT EXISTS volunteer_certification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    real_name VARCHAR(64),
    id_card_no VARCHAR(32),
    id_card_front_url VARCHAR(255),
    id_card_back_url VARCHAR(255),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    submit_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    audit_time DATETIME,
    auditor_id BIGINT,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_volunteer_certification_user (user_id),
    INDEX idx_volunteer_certification_status (status),
    INDEX idx_volunteer_certification_submit_time (submit_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO volunteer_certification (
    user_id,
    real_name,
    status,
    submit_time,
    audit_time,
    create_time,
    update_time
)
SELECT u.id,
       u.real_name,
       'APPROVED',
       COALESCE(u.create_time, NOW()),
       COALESCE(u.update_time, NOW()),
       COALESCE(u.create_time, NOW()),
       COALESCE(u.update_time, NOW())
FROM sys_user u
WHERE u.role = 'VOLUNTEER'
  AND u.certified = 1
  AND NOT EXISTS (
      SELECT 1
      FROM volunteer_certification vc
      WHERE vc.user_id = u.id
  );
