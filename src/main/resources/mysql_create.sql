-- All of the item and block events
CREATE TABLE IF NOT EXISTS ws_materials (
	material_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Who
	material_player INT UNSIGNED,
	-- What
	material_event SMALLINT UNSIGNED NOT NULL,
	material_type SMALLINT UNSIGNED NOT NULL,
	material_count SMALLINT UNSIGNED,
	-- When
	material_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	-- Where
	material_world SMALLINT UNSIGNED NOT NULL,
	material_x INT NOT NULL,
	material_y INT NOT NULL,
	material_z INT NOT NULL
);

-- What type of item or block event occurred
CREATE TABLE IF NOT EXISTS ws_material_events (
	event_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	event_name VARCHAR(255) UNIQUE
);

-- List of materials we are tracking
CREATE TABLE IF NOT EXISTS ws_material_types (
	type_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	type_name VARCHAR(255) UNIQUE NOT NULL,
	type_block BIT NOT NULL
);

-- List of worlds we are tracking
CREATE TABLE IF NOT EXISTS ws_worlds (
	world_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Data
	world_name VARCHAR(255) UNIQUE NOT NULL,
	world_uuid CHAR(36) UNIQUE NOT NULL,
	world_type SMALLINT UNSIGNED NOT NULL,
	world_environment SMALLINT UNSIGNED NOT NULL
);

-- Whether a world is terra, nether, ender
CREATE TABLE IF NOT EXISTS ws_world_environments (
	environment_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	environment_name VARCHAR(255) UNIQUE
);

-- Whether a world is normal, flat, large biomes, amplified
CREATE TABLE IF NOT EXISTS ws_world_types (
	type_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	type_name VARCHAR(255) UNIQUE
);

-- All of the damage events
CREATE TABLE IF NOT EXISTS ws_damages (
	damage_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Who
	damage_damager_type SMALLINT UNSIGNED,
	damage_damager_player INT UNSIGNED,
	damage_damager_material SMALLINT UNSIGNED,
	damage_damager_projectile SMALLINT UNSIGNED,
	damage_damaged_type SMALLINT UNSIGNED NOT NULL,
	damage_damaged_player INT UNSIGNED,
	damage_damaged_death BIT NOT NULL DEFAULT 0,
	-- What
	damage_type SMALLINT UNSIGNED NOT NULL,
	damage_amount FLOAT UNSIGNED NOT NULL,
	-- When
	damage_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	-- Where
	damage_world SMALLINT UNSIGNED NOT NULL,
	damage_x INT NOT NULL,
	damage_y INT NOT NULL,
	damage_z INT NOT NULL
);

-- List of damage causes we are tracking
CREATE TABLE IF NOT EXISTS ws_damage_types (
	damage_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	damage_name VARCHAR(255) UNIQUE
);

-- List of entities we are tracking
CREATE TABLE IF NOT EXISTS ws_entity_types (
	entity_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	entity_name VARCHAR(255) UNIQUE
);

-- All of the player move events
CREATE TABLE IF NOT EXISTS ws_distances (
	distance_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Who
	distance_player INT UNSIGNED NOT NULL,
	-- What
	distance_type SMALLINT UNSIGNED NOT NULL,
	distance_count FLOAT UNSIGNED NOT NULL,
	-- When
	distance_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	-- Where
	distance_world SMALLINT UNSIGNED NOT NULL,
	distance_from_x INT NOT NULL,
	distance_from_y INT NOT NULL,
	distance_from_z INT NOT NULL,
	distance_to_x INT NOT NULL,
	distance_to_y INT NOT NULL,
	distance_to_z INT NOT NULL
);

-- Distance types being tracked by us
CREATE TABLE IF NOT EXISTS ws_distance_types (
	distance_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	distance_name VARCHAR(255) UNIQUE
);

-- All of the egg throw events
CREATE TABLE IF NOT EXISTS ws_eggs (
	egg_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Who
	egg_player INT UNSIGNED,
	-- What
	egg_count TINYINT UNSIGNED,
	-- When
	egg_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	-- Where
	egg_world SMALLINT UNSIGNED NOT NULL,
	egg_x INT NOT NULL,
	egg_y INT NOT NULL,
	egg_z INT NOT NULL
);

-- All of the fishing events
CREATE TABLE IF NOT EXISTS ws_fishes (
	fish_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Who
	fish_player INT UNSIGNED NOT NULL,
	-- What
	fish_event SMALLINT UNSIGNED NOT NULL,
	fish_caught_entity SMALLINT UNSIGNED,
	fish_caught_player INT UNSIGNED,
	fish_caught_material SMALLINT UNSIGNED,
	-- When
	fish_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	-- Where
	fish_world SMALLINT UNSIGNED NOT NULL,
	fish_x INT NOT NULL,
	fish_y INT NOT NULL,
	fish_z INT NOT NULL
);

-- Fish states we are tracking
CREATE TABLE IF NOT EXISTS ws_fish_events (
	event_id SMALLINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Enum
	event_name VARCHAR(255) UNIQUE
);

-- All of the players we are tracking
CREATE TABLE IF NOT EXISTS ws_players (
	player_id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Data
	player_name CHAR(16) UNIQUE,
	player_uuid CHAR(36) UNIQUE
);

-- History of all sessions
CREATE TABLE IF NOT EXISTS ws_sessions (
	session_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Who
	session_player INT UNSIGNED NOT NULL,
	-- What
	session_expired BIT NOT NULL DEFAULT 0,
	-- When
	session_start DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	session_end DATETIME ON UPDATE CURRENT_TIMESTAMP
	-- Where
);

CREATE TABLE IF NOT EXISTS ws_uptimes (
	uptime_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Who
	-- What
	uptime_expired BIT NOT NULL DEFAULT 0,
	-- When
	uptime_start DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
	uptime_end DATETIME ON UPDATE CURRENT_TIMESTAMP
	-- Where
);

-- Includes peak players, maximum players
CREATE TABLE IF NOT EXISTS ws_statistics (
	statistic_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	-- Data
	statistic_key VARCHAR(255) UNIQUE,
	statistic_value VARCHAR(255)
);
