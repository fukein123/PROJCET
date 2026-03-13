INSERT INTO sys_user (id, username, password, email, phone, gender, avatar, role, status, real_name, certified)
VALUES (1, 'admin', '$2b$10$MdUnSo.DjQ85.dpOSxWW2eTv8RoIJNzNv1P7fcozYwpDsx3Bx6FQK', 'admin@cvs.local',
        '13800000000', 'UNKNOWN', 'https://cdn.jsdelivr.net/gh/fukexin123/assets/default-admin.png', 'ADMIN', 1, '社区管理员', 1)
AS seed
ON DUPLICATE KEY UPDATE username = seed.username,
                        email = seed.email,
                        phone = seed.phone,
                        gender = seed.gender,
                        avatar = seed.avatar,
                        role = seed.role,
                        status = seed.status,
                        real_name = seed.real_name,
                        certified = seed.certified;

INSERT INTO sys_user (id, username, password, email, phone, gender, avatar, role, status, real_name, certified)
VALUES (2, 'volunteer', '$2b$10$MdUnSo.DjQ85.dpOSxWW2eTv8RoIJNzNv1P7fcozYwpDsx3Bx6FQK', 'volunteer@cvs.local',
        '13900000000', 'UNKNOWN', 'https://cdn.jsdelivr.net/gh/fukexin123/assets/default-avatar.png', 'VOLUNTEER', 1,
        '李四', 1)
AS seed
ON DUPLICATE KEY UPDATE username = seed.username,
                        email = seed.email,
                        phone = seed.phone,
                        gender = seed.gender,
                        avatar = seed.avatar,
                        role = seed.role,
                        status = seed.status,
                        real_name = seed.real_name,
                        certified = seed.certified;

INSERT INTO activity_category (id, name, description, sort, status)
VALUES (1, '社区关爱', '面向社区老人和儿童开展便民服务与关爱行动', 1, 1),
       (2, '环境治理', '组织垃圾分类、河道清洁、绿化维护等服务', 2, 1),
       (3, '健康公益', '开展义诊宣教、急救培训和健康筛查', 3, 1),
       (4, '文明宣传', '推进社区文明倡议、政策宣传和志愿倡导', 4, 1),
       (5, '助学帮扶', '为青少年和困难群体提供学习与成长支持', 5, 1)
AS seed
ON DUPLICATE KEY UPDATE name = seed.name;

INSERT INTO forum_category (id, name, sort, status)
VALUES (1, '志愿心得', 1, 1),
       (2, '活动反馈', 2, 1),
       (3, '社区建议', 3, 1),
       (4, '暖心故事', 4, 1)
AS seed
ON DUPLICATE KEY UPDATE name = seed.name;

INSERT INTO notice_info (id, title, content, status, publish_time)
VALUES (1, '平台上线通知', '社区志愿服务平台已上线，欢迎管理员和志愿者使用。', 1, NOW()),
       (2, '报名审核说明', '报名后请耐心等待管理员审核，通过后可在活动时间内签到签退。', 1, DATE_SUB(NOW(), INTERVAL 1 DAY))
AS seed
ON DUPLICATE KEY UPDATE title = seed.title;

INSERT INTO info_dynamic (id, title, content, image_url, type, views, status, publish_time, author_id)
VALUES (1, '周末河道清洁行动开启报名', '本次活动计划招募 30 名志愿者，欢迎社区居民积极参与。',
        'https://images.unsplash.com/photo-1497436072909-60f360e1d4b1?auto=format&fit=crop&w=900&q=80',
        'DYNAMIC', 120, 1, NOW(), 1),
       (2, '社区志愿服务流程更新', '平台已升级为“活动报名-审核-打卡-反馈”闭环流程，并移除了旧激励模块。',
        'https://images.unsplash.com/photo-1532629345422-7515f3d16bb6?auto=format&fit=crop&w=900&q=80',
        'NEWS', 98, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), 1),
       (3, '青少年公益课堂本周开讲', '本周将开展阅读分享与科学小实验，欢迎志愿者报名协助。',
        'https://images.unsplash.com/photo-1529156069898-49953e39b3ac?auto=format&fit=crop&w=900&q=80',
        'NEWS', 74, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 1)
AS seed
ON DUPLICATE KEY UPDATE title = seed.title;

INSERT INTO activity (id, title, category_id, start_time, end_time, address, status, target_count, volunteer_quota, content,
                      description, cover_image, creator_id)
VALUES (1, '河道环保清洁行动', 2, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 2 HOUR,
        '合肥市包河区滨湖文化广场', 'PUBLISHED', 30, 30, '河道垃圾清理、分类回收与环保宣导',
        '活动内容包括垃圾清理、分类回收和环保宣传，请参与人员穿着便于户外行动的服装。',
        'https://images.unsplash.com/photo-1497436072909-60f360e1d4b1?auto=format&fit=crop&w=900&q=80', 1),
       (2, '社区老人手机课堂', 1, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 2 HOUR,
        '蜀山区幸福家园社区中心', 'PUBLISHED', 25, 18, '老年智能手机基础操作与防诈骗宣讲',
        '帮助社区老人学习手机支付、挂号和防诈骗知识，活动结束后收集反馈持续优化。',
        'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=900&q=80', 1),
       (3, '周末义诊与健康宣教', 3, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 3 HOUR,
        '社区卫生服务站', 'PUBLISHED', 20, 12, '基础义诊筛查、慢病咨询与健康知识讲解',
        '联合医护志愿者开展血压血糖检测、慢病咨询与健康知识宣教。',
        'https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=900&q=80', 1)
AS seed
ON DUPLICATE KEY UPDATE title = seed.title,
                        category_id = seed.category_id,
                        start_time = seed.start_time,
                        end_time = seed.end_time,
                        address = seed.address,
                        status = seed.status,
                        target_count = seed.target_count,
                        volunteer_quota = seed.volunteer_quota,
                        content = seed.content,
                        description = seed.description,
                        cover_image = seed.cover_image,
                        creator_id = seed.creator_id;

INSERT INTO banner_info (id, title, image_url, activity_id, sort, status)
VALUES (1, '河道环保清洁行动',
        'https://images.unsplash.com/photo-1497436072909-60f360e1d4b1?auto=format&fit=crop&w=1200&q=80', 1, 1, 1),
       (2, '社区老人手机课堂',
        'https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&w=1200&q=80', 2, 2, 1)
AS seed
ON DUPLICATE KEY UPDATE title = seed.title;

INSERT INTO forum_post (id, title, content, category_id, user_id, status, views)
VALUES (1, '第一次参加河道清洁行动的感受', '和居民一起完成了清理与分类，最大的收获是看见大家对环境变化的真实反馈。', 2, 2,
        'APPROVED', 66),
       (2, '手机课堂如何更适合老年人', '建议每次活动控制在 90 分钟以内，并准备纸质步骤卡，效果会更稳定。', 3, 2, 'APPROVED', 32),
       (3, '社区志愿服务中的沟通技巧', '活动前明确分工、活动中及时反馈、活动后复盘总结，是提升效率的关键。', 1, 2, 'APPROVED',
        25)
AS seed
ON DUPLICATE KEY UPDATE title = seed.title;

INSERT INTO activity_application (id, activity_id, user_id, status, reject_reason, apply_time, audit_time, auditor_id)
VALUES (1, 1, 2, 'APPROVED', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 1),
       (2, 2, 2, 'APPROVED', NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 1)
AS seed
ON DUPLICATE KEY UPDATE status = seed.status;

INSERT INTO activity_check_record (id, activity_id, user_id, sign_in_time, sign_out_time, status)
VALUES (1, 1, 2, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 110 MINUTE, 'FINISHED'),
       (2, 2, 2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 95 MINUTE, 'FINISHED')
AS seed
ON DUPLICATE KEY UPDATE sign_out_time = seed.sign_out_time;

INSERT INTO comment_info (id, target_type, target_id, user_id, content, status)
VALUES (1, 'ACTIVITY', 1, 2, '活动组织有序，物资准备充分，现场协作体验很好。', 1),
       (2, 'ACTIVITY', 2, 2, '老人反馈积极，建议增加一对一辅导环节。', 1),
       (3, 'POST', 1, 1, '内容很有启发，欢迎继续分享更多志愿经验。', 1)
AS seed
ON DUPLICATE KEY UPDATE content = seed.content;

DELETE FROM comment_info WHERE test_data_tag IS NOT NULL AND test_data_tag LIKE 'smoke:%';

