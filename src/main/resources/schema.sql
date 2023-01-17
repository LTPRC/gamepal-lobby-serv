-- DROP TABLE `user_info`;
CREATE TABLE if not exists `user_info` (
`id` long not null auto_increment,
`user_code` varchar(36) unique not null,
`username` varchar(50) not null,
`password` varchar(50) not null,
`status` integer not null DEFAULT 0,
`time_created` varchar(19) not null,
`time_updated` varchar(19) not null,
primary key (`id`),
unique key `user_code_key` (`user_code`) using btree
);

-- DROP TABLE `user_character`;
--CREATE TABLE if not exists `user_character` (
--`id` long not null auto_increment,
--`user_code` varchar(36) unique not null,
--`avatar` varchar(10),
--`full_name` varchar(100),
--`first_name` varchar(50),
--`last_name` varchar(50),
--`nickname` varchar(100),
--`name_color` varchar(10),
--`creature` varchar(10),
--`gender` varchar(10),
--`skin_color` varchar(10),
--`hairstyle` varchar(10),
--`hair_color` varchar(10),
--`eyes` varchar(10),
--`create_time` varchar(19) not null,
--`update_time` varchar(19) not null,
--primary key (`id`)
--);