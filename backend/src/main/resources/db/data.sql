INSERT INTO sys_user (id, username, password, email, phone, gender, avatar, role, status, real_name, certified)
VALUES (1, 'admin', '$2b$10$MdUnSo.DjQ85.dpOSxWW2eTv8RoIJNzNv1P7fcozYwpDsx3Bx6FQK', 'admin@cvs.local',
        '13800000000', 'UNKNOWN', 'https://cdn.jsdelivr.net/gh/fukexin123/assets/default-admin.png', 'ADMIN', 1,
        'SystemAdmin', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO sys_user (id, username, password, email, phone, gender, avatar, role, status, real_name, certified)
VALUES (2, 'volunteer', '$2b$10$MdUnSo.DjQ85.dpOSxWW2eTv8RoIJNzNv1P7fcozYwpDsx3Bx6FQK', 'volunteer@cvs.local',
        '13900000000', 'UNKNOWN', 'https://cdn.jsdelivr.net/gh/fukexin123/assets/default-avatar.png', 'VOLUNTEER', 1,
        'DefaultVolunteer', 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO activity_category (id, name, description, sort, status)
VALUES (1, 'Eco Cleaning', 'Community cleaning and environment campaign', 1, 1),
       (2, 'Senior Care', 'Companion service for senior residents', 2, 1),
       (3, 'Culture Promotion', 'Community civilization promotion', 3, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO forum_category (id, name, sort, status)
VALUES (1, 'Activity Notes', 1, 1),
       (2, 'Community Suggestions', 2, 1),
       (3, 'Mutual Help', 3, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO notice_info (id, title, content, status, publish_time)
VALUES (1, 'System Launch Notice', 'Community volunteer management system is online now.', 1, NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO info_dynamic (id, title, content, type, views, status, publish_time, author_id)
VALUES (1, 'Spring Community Cleaning Starts', 'Weekend cleaning event is now open for registration.', 'NEWS', 120, 1, NOW(), 1),
       (2, 'Volunteer Point Rules', 'Points are granted automatically after sign-in and sign-out.', 'DYNAMIC', 88, 1, NOW(), 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO activity (id, title, category_id, start_time, end_time, address, status, target_count, description, latitude,
                      longitude, creator_id)
VALUES (1, 'Weekend River Cleanup', 1, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR, 'Happiness Community River Square',
        'PUBLISHED', 30, 'Organize volunteers to clean river areas and promote green living.', 31.2304, 121.4737, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO banner_info (id, title, image_url, activity_id, sort, status)
VALUES (1, 'Weekend River Cleanup', 'https://images.unsplash.com/photo-1497436072909-60f360e1d4b1', 1, 1, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO forum_post (id, title, content, category_id, user_id, status, views)
VALUES (1, 'My First Volunteer Activity', 'Thanks to the community team. Great order and great experience.', 1, 2, 'APPROVED', 66)
ON DUPLICATE KEY UPDATE title = VALUES(title);
