DROP DATABASE IF EXISTS db12636;
CREATE DATABASE db12636;
USE db12636;

CREATE TABLE users_pass (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    pass BINARY(16) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE users_email (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    email VARBINARY(254) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    surname VARCHAR(30) NOT NULL,
    pfp BLOB,
    email BIGINT UNSIGNED NOT NULL UNIQUE,
    pass BIGINT UNSIGNED UNIQUE,
    is_google BOOL NOT NULL DEFAULT FALSE,
    CHECK ((is_google AND pass IS NULL) OR (NOT is_google AND pass IS NOT NULL)),
    PRIMARY KEY (id),
    FOREIGN KEY (email) REFERENCES users_email(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    FOREIGN KEY (pass) REFERENCES users_pass(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);

CREATE TABLE users_poi (
    user_id BIGINT UNSIGNED,
    osm_id BIGINT UNSIGNED,
    does_like BOOL NOT NULL DEFAULT FALSE,
    favourite BOOL NOT NULL DEFAULT FALSE,
    comment VARCHAR(255),
    PRIMARY KEY (user_id, osm_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);

CREATE TABLE users_reserve_poi (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    user_id BIGINT UNSIGNED,
    osm_id BIGINT UNSIGNED,
    start DATE NOT NULL,
    end DATE NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);

CREATE PROCEDURE insert_user(@name VARCHAR(30), @surname VARCHAR(30), @pfp BLOB, @email VARCHAR(254), @pass BINARY(16))
BEGIN
    DECLARE @pass_id BIGINT UNSIGNED;
    DECLARE @email_id BIGINT UNSIGNED;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
        END;
    START TRANSACTION;
    INSERT INTO users_pass(pass)
    VALUES (@pass);
    SET @pass_id = LAST_INSERT_ID();
    INSERT INTO users_email(email)
    VALUES (@email);
    SET @email_id = LAST_INSERT_ID();
    INSERT INTO users(name, surname, pfp, email, pass)
    VALUES (@name, @surname, @pfp, @email_id, @pass_id);
    COMMIT;
END;

CREATE FUNCTION get_user_info(@email VARCHAR(254), OUT @pass BINARY(16)) RETURNS BIGINT UNSIGNED
BEGIN
    DECLARE uid BIGINT UNSIGNED;
    SELECT  uid = u.id, @pass = up.pass
    FROM    users AS u
            INNER JOIN users_pass AS up ON u.pass = up.id
            INNER JOIN users_email AS ue ON u.email = ue.id
    WHERE   ue.email = @email;
END;