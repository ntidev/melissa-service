CREATE TABLE IF NOT EXISTS `token` (
  `id` char(36) NOT NULL,
  `application_id` char(36) NOT NULL,
  `username` varchar(45) NOT NULL,
  `code` char(32) NOT NULL,
  `lifeend` bigint(11) NOT NULL,
  `created_at` bigint(11) NOT NULL,
  `connected` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;


CREATE TABLE IF NOT EXISTS `application` (
  `id` char(36) NOT NULL,
  `name` varchar(10) NOT NULL,
  `description` varchar(45) DEFAULT '',
  `enabled` tinyint(4) NOT NULL DEFAULT 1,
  `api_token` char(32) NOT NULL,
  `created_at` bigint(11) DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;