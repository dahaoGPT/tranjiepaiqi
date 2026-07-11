-- 测试数据：默认用户
insert into users (id, username, display_name, role, created_at)
values ('550e8400-e29b-41d4-a716-446655440000', 'default', '默认用户', 'FAMILY', '2024-01-01 00:00:00');

-- 测试数据：老人档案
insert into elders (id, name, notes, created_at)
values ('550e8400-e29b-41d4-a716-446655440001', '张奶奶', '独居老人，日常用水规律', '2024-01-01 00:00:00');

-- 测试数据：模拟器设备
insert into devices (id, elder_id, serial_number, status, last_seen_at, created_at)
values ('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'sim-device-001', 'OFFLINE', null, '2024-01-01 00:00:00');

-- 测试数据：用户绑定老人
insert into user_elder_bindings (user_id, elder_id)
values ('550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440001');