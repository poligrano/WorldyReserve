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
    email VARCHAR(254) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    gid BIGINT UNSIGNED,
    name VARCHAR(30) NOT NULL,
    surname VARCHAR(30) NOT NULL,
    pfp BLOB,
    email BIGINT UNSIGNED NOT NULL UNIQUE,
    pass BIGINT UNSIGNED UNIQUE,
    CHECK ((gid IS NULL AND pass IS NOT NULL) OR (gid IS NOT NULL AND pass IS NULL)),
    PRIMARY KEY (id),
    FOREIGN KEY (email) REFERENCES users_email(id),
    FOREIGN KEY (pass) REFERENCES users_pass(id)
);

CREATE UNIQUE INDEX google_id ON users(gid);

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

CREATE PROCEDURE INSERT_NORMAL_USER(v_name VARCHAR(30), v_surname VARCHAR(30), v_pfp BLOB, v_email VARCHAR(254), v_pass BINARY(16))
BEGIN
    DECLARE v_pass_id BIGINT UNSIGNED;
    DECLARE v_email_id BIGINT UNSIGNED;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
        END;
    START TRANSACTION;
    INSERT INTO users_pass(pass)
    VALUES (v_pass);
    SET v_pass_id = LAST_INSERT_ID();
    INSERT INTO users_email(email)
    VALUES (v_email);
    SET v_email_id = LAST_INSERT_ID();
    INSERT INTO users(name, surname, pfp, email, pass)
    VALUES (v_name, v_surname, v_pfp, v_email_id, v_pass_id);
    COMMIT;
END;

CREATE PROCEDURE INSERT_GOOGLE_USER(v_name VARCHAR(30), v_surname VARCHAR(30), v_pfp BLOB, v_email VARCHAR(254), v_gid BIGINT UNSIGNED)
BEGIN
    DECLARE v_email_id BIGINT UNSIGNED;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
        END;
    START TRANSACTION;
    INSERT INTO users_email(email)
    VALUES (v_email);
    SET v_email_id = LAST_INSERT_ID();
    INSERT INTO users(name, surname, pfp, email, gid)
    VALUES (v_name, v_surname, v_pfp, v_email_id, v_gid);
    COMMIT;
END;

CREATE PROCEDURE GET_NORMAL_USER_INFO(v_email VARCHAR(254), OUT v_pass BINARY(16), OUT v_id BIGINT UNSIGNED)
BEGIN
    SELECT  v_id = u.id, v_pass = up.pass
    FROM    users AS u
            INNER JOIN users_pass AS up ON u.pass = up.id
            INNER JOIN users_email AS ue ON u.email = ue.id
    WHERE   u.gid IS NULL
            AND ue.email = v_email;
END;