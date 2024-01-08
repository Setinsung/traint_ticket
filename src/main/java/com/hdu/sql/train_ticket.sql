/*
 Navicat Premium Data Transfer

 Source Server         : My_MySQL
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : train_ticket

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 08/01/2024 16:12:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '用户名称',
  `telephone` varchar(13) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '手机号',
  `mail` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '邮箱',
  `password` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '加密后的密码',
  `dept_id` int NOT NULL DEFAULT 0 COMMENT '用户所在部门的id',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态，1：正常，0：冻结状态，2：删除',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '备注',
  `operator` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '操作者',
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后一次更新时间',
  `operate_ip` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '最后一次更新者的ip地址',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_city
-- ----------------------------
DROP TABLE IF EXISTS `train_city`;
CREATE TABLE `train_city`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_number
-- ----------------------------
DROP TABLE IF EXISTS `train_number`;
CREATE TABLE `train_number`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '车次',
  `from_station_id` int NOT NULL DEFAULT 0 COMMENT '始发站',
  `from_city_id` int NOT NULL DEFAULT 0 COMMENT '始发城市',
  `to_station_id` int NOT NULL DEFAULT 0 COMMENT '终点站',
  `to_city_id` int NOT NULL DEFAULT 0 COMMENT '终点城市',
  `train_type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '座位类型，CRH2,CRH5',
  `type` smallint NOT NULL DEFAULT 0 COMMENT '类型，1：高铁，2：动车，3：特快，4：普快',
  `seat_num` int NOT NULL DEFAULT 0 COMMENT '车厢数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_number_detail
-- ----------------------------
DROP TABLE IF EXISTS `train_number_detail`;
CREATE TABLE `train_number_detail`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `train_number_id` int NOT NULL DEFAULT 0 COMMENT '车次',
  `from_station_id` int NOT NULL DEFAULT 0 COMMENT '上一站',
  `from_city_id` int NOT NULL DEFAULT 0 COMMENT '上一站所在的城市',
  `to_station_id` int NOT NULL COMMENT '到站',
  `to_city_id` int NOT NULL DEFAULT 0 COMMENT '到站所在的城市',
  `station_index` int NOT NULL DEFAULT 0 COMMENT '在整个车次中的顺序',
  `relative_minute` int NOT NULL DEFAULT 0 COMMENT '相对出发时间的分钟数',
  `wait_minute` int NOT NULL DEFAULT 0 COMMENT '到此站等待时间',
  `money` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '价格',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_numberid_index`(`train_number_id` ASC, `station_index` ASC) USING BTREE,
  UNIQUE INDEX `uniq_numberid_from`(`train_number_id` ASC, `from_station_id` ASC) USING BTREE,
  UNIQUE INDEX `uniq_numberid_to`(`train_number_id` ASC, `to_station_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 248 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_seat
-- ----------------------------
DROP TABLE IF EXISTS `train_seat`;
CREATE TABLE `train_seat`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ticket` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '日期，yyyyMMdd',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '登录用户id',
  `traveller_id` bigint NOT NULL DEFAULT 0 COMMENT '绑定的乘客id',
  `train_number_id` int NOT NULL DEFAULT 0 COMMENT '车次',
  `carriage_number` int NOT NULL COMMENT '车厢',
  `row_number` int NOT NULL DEFAULT 0 COMMENT '排',
  `seat_number` int NOT NULL DEFAULT 0 COMMENT 'A~F',
  `seat_level` int NOT NULL DEFAULT 2 COMMENT '座位等级，0：特等座，1：一等座，2：二等座，3：无座',
  `train_start` datetime NOT NULL COMMENT '发车时间',
  `train_end` datetime NOT NULL COMMENT '到达时间',
  `money` int NOT NULL DEFAULT 0 COMMENT '价格',
  `show_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '展示',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态，0:初始，1：已放票，2：占有票等待支付，3：已支付，4：不外放',
  `from_station_id` int NOT NULL DEFAULT 0,
  `to_station_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_trainnumberid_ticket`(`train_number_id` ASC, `ticket` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_station
-- ----------------------------
DROP TABLE IF EXISTS `train_station`;
CREATE TABLE `train_station`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '',
  `city_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_cityid`(`city_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_traveller
-- ----------------------------
DROP TABLE IF EXISTS `train_traveller`;
CREATE TABLE `train_traveller`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '名字',
  `adult_flag` tinyint NOT NULL DEFAULT 0 COMMENT '成人标识，0:成人，1:儿童',
  `sex` tinyint NOT NULL DEFAULT 0 COMMENT '性别，0:未知，1:男，2:女',
  `id_type` smallint NOT NULL COMMENT '证件类型，0:未知，1：身份证，2：护照',
  `id_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '证件号码',
  `contact` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '联系方式',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '联系地址',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '邮箱',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_user
-- ----------------------------
DROP TABLE IF EXISTS `train_user`;
CREATE TABLE `train_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '姓名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '密码',
  `telephone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '手机号',
  `mail` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL DEFAULT '' COMMENT '邮箱',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态，0:申请中，1：审核通过，2：禁止登陆',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_name`(`name` ASC) USING BTREE,
  UNIQUE INDEX `idx_telephone`(`telephone` ASC) USING BTREE,
  UNIQUE INDEX `idx_mail`(`mail` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for train_user_traveller
-- ----------------------------
DROP TABLE IF EXISTS `train_user_traveller`;
CREATE TABLE `train_user_traveller`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL DEFAULT 0,
  `traveller_id` bigint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userid`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
