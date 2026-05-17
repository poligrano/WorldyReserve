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
    FOREIGN KEY (email) REFERENCES users_email(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT,
    FOREIGN KEY (pass) REFERENCES users_pass(id)
        ON DELETE SET NULL
        ON UPDATE RESTRICT
);

CREATE UNIQUE INDEX google_id ON users(gid);

CREATE TABLE users_verify (
    id BIGINT UNSIGNED,
    code BINARY(16) NOT NULL DEFAULT UNHEX(SYS_GUID()),
    expiration TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL 20 MINUTE),
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE RESTRICT
);

CREATE UNIQUE INDEX confirm_code ON users_verify(code);

CREATE EVENT purge_non_verified
ON SCHEDULE EVERY 10 MINUTE
DO
BEGIN
    DECLARE v_now TIMESTAMP DEFAULT NOW();
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
        END;
    START TRANSACTION;
    DELETE FROM  users
    WHERE   id IN
    (
        SELECT  uv.id
        FROM    users_verify AS uv
        WHERE   uv.expiration < v_now
    );
    DELETE FROM users_verify
    WHERE   expiration < v_now;
    COMMIT;
END;

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

CREATE PROCEDURE INSERT_NORMAL_USER(v_name VARCHAR(30), v_surname VARCHAR(30), v_pfp BLOB, v_email VARCHAR(254), v_pass BINARY(16), OUT v_code CHAR(32))
BEGIN
    DECLARE v_pass_id BIGINT UNSIGNED;
    DECLARE v_email_id BIGINT UNSIGNED;
    DECLARE v_user_id BIGINT UNSIGNED;
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
    SET v_user_id = LAST_INSERT_ID();
    SET v_code = SYS_GUID();
    INSERT INTO users_verify(id, code)
    VALUES (v_user_id, UNHEX(v_code));
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
            AND ue.email = v_email
            AND u.id NOT IN
            (
                SELECT  uv.id
                FROM    users_verify AS uv
            );
END;

CREATE PROCEDURE VERIFY_USER(v_code CHAR(32), v_now TIMESTAMP, OUT v_sc TINYINT)
BEGIN
    DECLARE v_exp TIMESTAMP;
    DECLARE v_byte_code BINARY(16);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
        END;
    START TRANSACTION;
    SET v_byte_code = UNHEX(v_code);
    SELECT  uv.expiration INTO v_exp
    FROM    users_verify AS uv
    WHERE   uv.code = v_byte_code;
    IF v_exp IS NULL THEN
        SET v_sc = 0;
    ELSEIF v_exp < v_now THEN
        v_sc = -1;
    ELSE
        DELETE FROM users_verify
        WHERE   code = v_byte_code;
        v_sc = ROW_COUNT();
    END IF;
    COMMIT;
END;