INSERT IGNORE INTO `pdm_tool_sys_user` (`id`, `username`, `password`, `email`, `real_name`, `status`) VALUES
(2, 'manager',   '$2a$10$zOC1QKh3h501.9Rtb0u07uNl4RNJCtYI5vNmN2.HKuj..y3vQycDS', 'manager@example.com',   '部门经理', 1),
(3, 'testuser',  '$2a$10$prtWSl2japNQxDSrmDEQlOYpd3M8kboPpCZH5JBpBy.K8YVpSmZpW', 'testuser@example.com',  '测试用户', 1),
(4, 'developer', '$2a$10$5s.leuRFRjbnH1bD9NQVme3PkQ7SWxzZv0KxeO6T6qXuQBLcHfwzS', 'developer@example.com', '开发者',   1),
(5, 'viewer',    '$2a$10$5DSzdCnYITbvGEr/Qnt0xOEollmMp43ahXkc6N2./kOcDB5OAwoZu', 'viewer@example.com',    '只读用户', 1);


INSERT IGNORE INTO `pdm_tool_sys_user_role` (`id`, `user_id`, `role_id`) VALUES
('ur_manager',   '2', 'manager'),
('ur_testuser',  '3', 'user'),
('ur_developer', '4', 'user'),
('ur_viewer',    '5', 'user');