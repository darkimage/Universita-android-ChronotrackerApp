CREATE TABLE IF NOT EXISTS `activity` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS `activity_type` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(10) NOT NULL,
  `activity` INTEGER,
  FOREIGN KEY (`activity`) REFERENCES `activity`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `athlete` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(20) NOT NULL,
  `surname` TEXT(20) NOT NULL,
  `activity` INTEGER,
  FOREIGN KEY (`activity`) REFERENCES `activity`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `unit`(
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(20) NOT NULL,
  `short_name` TEXT(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS `tracked_session` (
   `id` INTEGER PRIMARY KEY AUTOINCREMENT,
   `athlete` INTEGER NOT NULL,
   `activity` INTEGER NOT NULL,
   `activity_type` INTEGER,
   `start_time` INTEGER NOT NULL,
   `stop_time` INTEGER NOT NULL,
   `distance` INTEGER NOT NULL,
   `speed` INTEGER NOT NULL,
   FOREIGN KEY (`athlete`) REFERENCES `athlete`(`id`) ON DELETE CASCADE,
   FOREIGN KEY (`activity`) REFERENCES `activity`(`id`) ON DELETE CASCADE,
   FOREIGN KEY (`distance`) REFERENCES `unit`(`id`) ON DELETE CASCADE,
   FOREIGN KEY (`activity_type`) REFERENCES `activity_type`(`id`) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `laps` (
   `id` INTEGER PRIMARY KEY AUTOINCREMENT,
   `lap_from_start` INTEGER NOT NULL,
   `lap_duration` INTEGER NOT NULL,
   `of_session` INTEGER NOT NULL,
    FOREIGN KEY (`of_session`) REFERENCES `tracked_session`(`id`) ON DELETE CASCADE
);