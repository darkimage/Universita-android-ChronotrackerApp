CREATE TABLE IF NOT EXISTS `activity` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS `activity_type` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(10) NOT NULL,
  `activity` INTEGER,
  FOREIGN KEY (`activity`) REFERENCES `activity`(`id`)
);

CREATE TABLE IF NOT EXISTS `athlete` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(20) NOT NULL,
  `surname` TEXT(20) NOT NULL,
  `activity` INTEGER,
  FOREIGN KEY (`activity`) REFERENCES `activity`(`id`)
);

CREATE TABLE IF NOT EXISTS `unit`(
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT(20) NOT NULL
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
   FOREIGN KEY (`athlete`) REFERENCES `athlete`(`id`),
   FOREIGN KEY (`activity`) REFERENCES `activity`(`id`),
   FOREIGN KEY (`activity_type`) REFERENCES `activity_type`(`id`)
);

CREATE TABLE IF NOT EXISTS `laps` (
   `id` INTEGER PRIMARY KEY AUTOINCREMENT,
   `lap_time` INTEGER NOT NULL,
   `of_session` INTEGER NOT NULL,
    FOREIGN KEY (`of_session`) REFERENCES `tracked_session`(`id`)
);