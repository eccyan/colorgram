SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

CREATE SCHEMA IF NOT EXISTS `colorgram` DEFAULT CHARACTER SET utf8 ;
USE `colorgram` ;

-- -----------------------------------------------------
-- Table `colorgram`.`photo_datas`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `colorgram`.`photo_datas` ;

CREATE  TABLE IF NOT EXISTS `colorgram`.`photo_datas` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `pid` CHAR(40) NOT NULL COMMENT 'Parmanent ID' ,
  `uid` BIGINT(20) NULL DEFAULT NULL COMMENT 'User ID' ,
  `uid_string` CHAR(40) NULL DEFAULT NULL COMMENT 'User ID String' ,
  `url` CHAR(100) NOT NULL ,
  `hue` FLOAT NOT NULL DEFAULT 0.0 ,
  `saturation` FLOAT NOT NULL DEFAULT 0.0 ,
  `brightness` FLOAT NOT NULL DEFAULT 0.0 ,
  `status_code` INT UNSIGNED NULL DEFAULT NULL ,
  `tried_access_count` INT UNSIGNED NOT NULL DEFAULT 0 ,
  `created` DATETIME NULL DEFAULT NULL ,
  `modified` DATETIME NULL DEFAULT NULL ,
  INDEX `idx_photo_datas1` (`status_code` ASC, `created` ASC) ,
  INDEX `idx_photo_datas2` (`status_code` ASC, `hue` ASC) ,
  INDEX `idx_photo_datas3` (`status_code` ASC, `saturation` ASC) ,
  INDEX `idx_photo_datas4` (`status_code` ASC, `brightness` ASC) ,
  INDEX `idx_photo_datas5` (`uid` ASC) ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
