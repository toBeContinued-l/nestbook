-- MySQL 5.7.44 compatible DDL. Use utf8mb4 for WeChat text and nicknames.
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS t_family (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(64) NOT NULL COMMENT '家庭名称',
  creator_user_id BIGINT NULL COMMENT '创建家庭的用户ID',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  trial_end_time DATETIME NOT NULL COMMENT '体验期结束时间',
  is_activated TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已激活：0否，1是',
  activated_time DATETIME NULL COMMENT '激活时间',
  daily_ai_limit INT NOT NULL DEFAULT 10 COMMENT '每日AI解析次数上限',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '家庭状态：active正常',
  KEY idx_family_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='家庭表';

CREATE TABLE IF NOT EXISTS t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  oa_openid VARCHAR(128) NULL COMMENT '微信公众号OpenID',
  mp_openid VARCHAR(128) NULL COMMENT '微信小程序OpenID',
  nickname VARCHAR(64) NOT NULL COMMENT '用户昵称',
  role VARCHAR(32) NOT NULL DEFAULT 'member' COMMENT '用户角色：member成员',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '用户状态：active正常',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_user_oa_openid (oa_openid),
  UNIQUE KEY uk_user_mp_openid (mp_openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='内部用户表，仅保存全局身份信息';

CREATE TABLE IF NOT EXISTS t_family_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  family_id BIGINT NOT NULL COMMENT '家庭ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  nickname VARCHAR(64) NOT NULL COMMENT '家庭内显示昵称',
  role VARCHAR(32) NOT NULL DEFAULT 'member' COMMENT '家庭角色：member成员',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '成员状态：active正常',
  join_time DATETIME NOT NULL COMMENT '加入家庭时间',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_family_member_user (user_id),
  KEY idx_family_member_family_id (family_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='家庭成员表；唯一用户索引限制MVP阶段单用户单家庭';

CREATE TABLE IF NOT EXISTS t_bill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  family_id BIGINT NOT NULL COMMENT '所属家庭ID',
  user_id BIGINT NOT NULL COMMENT '记账用户ID',
  oa_openid VARCHAR(128) NOT NULL COMMENT '记账来源的公众号OpenID',
  bill_type TINYINT NOT NULL COMMENT '账单类型：1收入，2支出',
  amount DECIMAL(10, 2) NOT NULL COMMENT '金额，单位元',
  category VARCHAR(64) NOT NULL COMMENT '账单分类',
  bill_date DATE NOT NULL COMMENT '账单日期',
  remark VARCHAR(255) NULL COMMENT '账单备注',
  raw_text VARCHAR(512) NOT NULL COMMENT '用户原始记账文本',
  wechat_msg_id VARCHAR(128) NOT NULL COMMENT '微信消息ID，用于幂等',
  source VARCHAR(32) NOT NULL DEFAULT 'wechat_oa' COMMENT '记账来源：wechat_oa公众号',
  parse_model VARCHAR(64) NULL COMMENT '解析使用的AI模型标识',
  confidence DECIMAL(4, 3) NULL COMMENT 'AI解析置信度，范围0到1',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否逻辑删除：0否，1是',
  deleted_time DATETIME NULL COMMENT '逻辑删除时间',
  deleted_by_user_id BIGINT NULL COMMENT '执行删除的用户ID',
  UNIQUE KEY uk_bill_oa_msg (oa_openid, wechat_msg_id),
  KEY idx_bill_family_active_date (family_id, is_deleted, bill_date),
  KEY idx_bill_user_active_created (user_id, is_deleted, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='账单主表';

CREATE TABLE IF NOT EXISTS t_wechat_message_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  oa_openid VARCHAR(128) NOT NULL COMMENT '发送消息的公众号OpenID',
  wechat_msg_id VARCHAR(128) NOT NULL COMMENT '微信消息ID',
  raw_text VARCHAR(512) NULL COMMENT '消息原文',
  message_type VARCHAR(32) NOT NULL COMMENT '微信消息类型',
  handle_type VARCHAR(32) NOT NULL COMMENT '处理类型：命令或AI解析等',
  handle_status VARCHAR(32) NOT NULL COMMENT '处理状态',
  error_message VARCHAR(512) NULL COMMENT '处理失败原因',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_wechat_message (oa_openid, wechat_msg_id),
  KEY idx_wechat_message_openid_time (oa_openid, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='微信公众号消息处理日志表';

CREATE TABLE IF NOT EXISTS t_ai_parse_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  family_id BIGINT NOT NULL COMMENT '所属家庭ID',
  user_id BIGINT NOT NULL COMMENT '发起解析的用户ID',
  raw_text VARCHAR(512) NOT NULL COMMENT '待解析的原始文本',
  prompt_version VARCHAR(32) NOT NULL COMMENT '提示词版本',
  model VARCHAR(64) NOT NULL COMMENT 'AI模型标识',
  request_json TEXT NOT NULL COMMENT '脱敏后的AI请求内容',
  response_json TEXT NULL COMMENT '脱敏后的AI响应内容',
  parse_status VARCHAR(32) NOT NULL COMMENT '解析状态：success成功，failed失败',
  token_input INT NULL COMMENT '输入Token数量',
  token_output INT NULL COMMENT '输出Token数量',
  cost DECIMAL(10, 4) NULL COMMENT '本次调用成本',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  KEY idx_ai_parse_log_family_time (family_id, create_time),
  KEY idx_ai_parse_log_status_time (parse_status, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI解析调用日志表，不保存密钥';

CREATE TABLE IF NOT EXISTS t_binding_token (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  token_hash VARCHAR(128) NOT NULL COMMENT '一次性绑定Token的哈希值，不存明文',
  oa_openid VARCHAR(128) NOT NULL COMMENT '发起绑定的公众号OpenID',
  oa_user_id BIGINT NOT NULL COMMENT '发起绑定的公众号用户ID',
  family_id BIGINT NOT NULL COMMENT '绑定时所属家庭ID',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态：active有效，used已使用，expired已过期',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  used_time DATETIME NULL COMMENT '使用时间',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_binding_token_hash (token_hash),
  KEY idx_binding_token_oa_user_status_expire (oa_user_id, status, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='公众号与小程序账号的一次性绑定凭证表';

CREATE TABLE IF NOT EXISTS t_invitation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  family_id BIGINT NOT NULL COMMENT '被邀请加入的家庭ID',
  inviter_user_id BIGINT NOT NULL COMMENT '邀请人用户ID',
  invite_code_hash VARCHAR(128) NOT NULL COMMENT '邀请码哈希值，不存明文',
  status VARCHAR(32) NOT NULL DEFAULT 'active' COMMENT '状态：active有效，expired已过期，disabled已失效',
  expire_time DATETIME NOT NULL COMMENT '过期时间',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_invitation_code_hash (invite_code_hash),
  KEY idx_invitation_family_status_expire (family_id, status, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='家庭邀请码表';

CREATE TABLE IF NOT EXISTS t_activation_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  code_hash VARCHAR(128) NOT NULL COMMENT '激活码哈希值，不存明文',
  status VARCHAR(32) NOT NULL DEFAULT 'unused' COMMENT '状态：unused未使用，used已使用',
  family_id BIGINT NULL COMMENT '兑换激活码的家庭ID',
  used_time DATETIME NULL COMMENT '兑换时间',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  UNIQUE KEY uk_activation_code_hash (code_hash),
  KEY idx_activation_code_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='人工发放的激活码表';

CREATE TABLE IF NOT EXISTS t_quota_usage_daily (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  family_id BIGINT NOT NULL COMMENT '家庭ID',
  usage_date DATE NOT NULL COMMENT '额度统计日期',
  ai_used_count INT NOT NULL DEFAULT 0 COMMENT '当日已使用AI解析次数',
  create_time DATETIME NOT NULL COMMENT '创建时间',
  update_time DATETIME NOT NULL COMMENT '更新时间',
  UNIQUE KEY uk_quota_family_date (family_id, usage_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='家庭每日AI解析额度使用量表';
