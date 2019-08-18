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
)
