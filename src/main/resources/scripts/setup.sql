DROP DATABASE IF EXISTS db12636;
CREATE DATABASE db12636;
USE db12636;

CREATE TABLE users_pass (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    pass BINARY(57) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE users_email (
     id BIGINT UNSIGNED AUTO_INCREMENT,
     email VARCHAR(254) NOT NULL UNIQUE,
     PRIMARY KEY (id)
);

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    gid VARCHAR(255) COLLATE utf8mb4_bin,
    name VARCHAR(30) NOT NULL,
    surname VARCHAR(30) NOT NULL,
    pfp BLOB,
    email BIGINT UNSIGNED NOT NULL UNIQUE,
    pass BIGINT UNSIGNED UNIQUE,
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


CREATE TABLE users_pass_change (
    id BIGINT UNSIGNED,
    code CHAR(8) NOT NULL,
    expiration TIMESTAMP DEFAULT (NOW() + INTERVAL 5 MINUTE),
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES users_pass(id)
       ON DELETE CASCADE
       ON UPDATE RESTRICT
);


CREATE TABLE users_poi (
    user_id BIGINT UNSIGNED,
    osm_id BIGINT UNSIGNED,
    does_like BOOL NOT NULL DEFAULT FALSE,
    favourite BOOL NOT NULL DEFAULT FALSE,
    last_interacted TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, osm_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
       ON DELETE CASCADE
       ON UPDATE RESTRICT
);

CREATE TABLE users_poi_comment (
    id TINYINT UNSIGNED AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    osm_id BIGINT UNSIGNED NOT NULL,
    comment VARCHAR(255) NOT NULL,
    when_posted TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
       ON DELETE CASCADE
       ON UPDATE RESTRICT
);

CREATE TABLE users_reserve_poi (
    id BIGINT UNSIGNED AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    osm_id BIGINT UNSIGNED NOT NULL,
    start TIMESTAMP NOT NULL,
    end TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES users(id)
       ON DELETE CASCADE
       ON UPDATE RESTRICT
);

DELIMITER //

CREATE EVENT purge_unused_code
    ON SCHEDULE EVERY 1 DAY
    DO
    CALL purge_unused_code()//

CREATE PROCEDURE purge_unused_code()
BEGIN
    DELETE FROM users_pass_change
    WHERE   expiration < NOW();
END//

CREATE TRIGGER check_reservation
    BEFORE INSERT
    ON users_reserve_poi FOR EACH ROW
BEGIN
    DECLARE v_last_reserve DATE;
    SELECT  urp.end INTO v_last_reserve
    FROM    users_reserve_poi AS urp
    WHERE   urp.osm_id = NEW.osm_id
            AND urp.user_id = NEW.user_id
    ORDER BY    urp.end DESC
    LIMIT 1;
    IF  NEW.start < NOW()
        OR NEW.end < NEW.start
        OR v_last_reserve > NOW() THEN
            SIGNAL SQLSTATE "45000"
                SET MESSAGE_TEXT = "Invalid reservation";
    END IF;
END//

CREATE EVENT purge_non_verified
    ON SCHEDULE EVERY 10 MINUTE
    DO
    BEGIN
        CALL purge_non_verified();
    END//

CREATE PROCEDURE purge_non_verified()
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
END//

CREATE PROCEDURE INSERT_NORMAL_USER(v_name TYPE OF users.name, v_surname TYPE OF users.surname, v_pfp TYPE OF users.pfp, v_email TYPE OF users_email.email, v_pass TYPE OF users_pass.pass, OUT v_code CHAR(32))
BEGIN
    DECLARE v_pass_id TYPE OF users_pass.id;
    DECLARE v_email_id TYPE OF users_email.id;
    DECLARE v_user_id TYPE OF users.id;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
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
END//

CREATE PROCEDURE INSERT_GOOGLE_USER(v_name TYPE OF users.name, v_surname TYPE OF users.surname, v_pfp TYPE OF users.pfp, v_email TYPE OF users_email.email, v_gid TYPE OF users.gid, OUT v_id TYPE OF users.id)
BEGIN
    DECLARE v_email_id TYPE OF users_email.id;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    INSERT INTO users_email(email)
    VALUES (v_email);
    SET v_email_id = LAST_INSERT_ID();
    INSERT INTO users(name, surname, pfp, email, gid)
    VALUES (v_name, v_surname, v_pfp, v_email_id, v_gid);
    SET v_id = LAST_INSERT_ID();
    COMMIT;
END//

CREATE PROCEDURE GET_NORMAL_USER_INFO(v_email TYPE OF users_email.email, OUT v_pass TYPE OF users_pass.pass, OUT v_id TYPE OF users.id)
BEGIN
    SELECT  u.id, up.pass INTO v_id, v_pass
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
END//

CREATE PROCEDURE GET_GOOGLE_USER_INFO(v_gid TYPE OF users.gid, OUT v_id TYPE OF users.id)
BEGIN
    SELECT  u.id INTO v_id
    FROM    users AS u
    WHERE   u.gid = v_gid;
END//

CREATE PROCEDURE VERIFY_USER(v_code CHAR(32), v_now TIMESTAMP, OUT v_sc TINYINT)
BEGIN
    DECLARE v_exp TIMESTAMP;
    DECLARE v_byte_code BINARY(16);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    SET v_byte_code = UNHEX(v_code);
    SELECT  uv.expiration INTO v_exp
    FROM    users_verify AS uv
    WHERE   uv.code = v_byte_code;
    CASE
        WHEN v_exp IS NULL THEN
            SET v_sc = 0;
        WHEN v_exp < v_now THEN
            SET v_sc = -1;
        ELSE
            BEGIN
                DELETE FROM users_verify
                WHERE   code = v_byte_code;
                SET v_sc = 1;
            END;
        END CASE;
    COMMIT;
END//

CREATE PROCEDURE SET_CHANGE_PASS_CODE(v_email TYPE OF users_email.email, v_code TYPE OF users_pass_change.code)
BEGIN
    DECLARE v_upid TYPE OF users_pass.id;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    SELECT  up.id INTO v_upid
    FROM    users AS u
            INNER JOIN users_email AS ue ON u.email = ue.id
            INNER JOIN users_pass up ON u.pass = up.id
    WHERE   ue.email = v_email
            AND u.id NOT IN
              (
                  SELECT  uv.id
                  FROM    users_verify AS uv
              );
    INSERT INTO users_pass_change(id, code)
    VALUES  (v_upid, v_code)
    ON DUPLICATE KEY UPDATE code = v_code, expiration = (NOW() + INTERVAL 5 MINUTE);
    COMMIT;
END//

CREATE PROCEDURE CHANGE_PASS(v_email TYPE OF users_email.email, v_now TIMESTAMP, v_code TYPE OF users_pass_change.code, v_pass TYPE OF users_pass.pass, OUT v_sc TINYINT)
BEGIN
    DECLARE v_upid TYPE OF users_pass.id;
    DECLARE v_exp TIMESTAMP;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;
    START TRANSACTION;
    SELECT  up.id INTO v_upid
    FROM    users AS u
            INNER JOIN users_email AS ue ON u.email = ue.id
            INNER JOIN users_pass up ON u.pass = up.id
    WHERE   ue.email = v_email;
    SELECT  upc.expiration INTO v_exp
    FROM    users_pass_change AS upc
    WHERE   upc.id = v_upid
      AND upc.code = v_code;
    CASE
        WHEN v_exp IS NULL THEN
            SET v_sc = 0;
        WHEN v_exp < v_now THEN
            SET v_sc = -1;
        ELSE
            BEGIN
                DELETE FROM users_pass_change
                WHERE   id = v_upid;
                UPDATE  users_pass
                SET pass = v_pass
                WHERE   id = v_upid;
                SET v_sc = 1;
            END;
        END CASE;
    COMMIT;
END//

CREATE PROCEDURE GET_USER_PFP(v_id TYPE OF users.id, OUT v_pfp TYPE OF users.pfp)
BEGIN
    SELECT  u.pfp INTO v_pfp
    FROM    users AS u
    WHERE   u.id = v_id
            AND u.id NOT IN
              (
                  SELECT  uv.id
                  FROM    users_verify AS uv
              );
END//

CREATE PROCEDURE GET_USER_POI_META(v_id TYPE OF users.id, v_osm_id TYPE OF users_poi.osm_id, OUT v_does_like TYPE OF users_poi.does_like, OUT v_favourite TYPE OF users_poi.favourite, OUT v_reserved BOOL)
BEGIN
    SELECT  up.does_like, up.favourite INTO v_does_like, v_favourite
    FROM    users_poi AS up
    WHERE   up.user_id = v_id
      AND up.osm_id = v_osm_id;
    SET v_reserved = NOW() < ANY (
        SELECT  urp.end
        FROM    users_reserve_poi AS urp
        WHERE   urp.user_id = v_id
                AND urp.osm_id = v_osm_id
    );
END//

CREATE PROCEDURE GET_POI_META(v_osm_id TYPE OF users_poi.osm_id, OUT v_like_number BIGINT UNSIGNED)
BEGIN
    SELECT  COUNT(up.does_like) INTO v_like_number
    FROM    users_poi AS up
    WHERE   up.osm_id = v_osm_id
            AND up.does_like = TRUE;
END//

CREATE PROCEDURE CHANGE_USER_POI_REL(v_osm_id TYPE OF users_poi.osm_id, v_id TYPE OF users.id, v_does_like TYPE OF users_poi.does_like, v_favourite TYPE OF users_poi.favourite)
BEGIN
    INSERT INTO users_poi(user_id, osm_id, does_like, favourite)
    VALUES (v_id, v_osm_id, v_does_like, v_favourite)
    ON DUPLICATE KEY UPDATE does_like = v_does_like, favourite = v_favourite, last_interacted = NOW();
END//

CREATE PROCEDURE POST_COMMENT(v_osm_id TYPE OF users_poi.osm_id, v_id TYPE OF users.id, v_comment TYPE OF users_poi_comment.comment)
BEGIN
    INSERT INTO users_poi_comment(user_id, osm_id, comment)
    VALUES (v_id, v_osm_id, v_comment);
END//

CREATE PROCEDURE RESERVE_POI(v_osm_id TYPE OF users_reserve_poi.osm_id, v_id TYPE OF users.id, v_start TYPE OF users_reserve_poi.start, v_end TYPE OF users_reserve_poi.end, OUT v_reservation_id TYPE OF users_reserve_poi.id)
BEGIN
    INSERT INTO users_reserve_poi(user_id, osm_id, start, end)
    VALUES (v_id, v_osm_id, v_start, v_end);
    SET v_reservation_id = LAST_INSERT_ID();
END//

CREATE PROCEDURE DELETE_RESERVATION(v_id TYPE OF users_reserve_poi.id, v_user_id TYPE OF users.id)
BEGIN
    DELETE FROM users_reserve_poi
    WHERE   id = v_id
            AND user_id = v_user_id;
END//

CREATE PROCEDURE GET_USER_MAIL(v_id TYPE OF users.id, OUT v_email TYPE OF users_email.email)
BEGIN
    SELECT  ue.email INTO v_email
    FROM    users AS u
            INNER JOIN users_email AS ue ON u.email = ue.id
    WHERE   u.id = v_id;
END//

CREATE PROCEDURE GET_USER_DISPLAY_NAME(v_id TYPE OF users.id, OUT v_name TYPE OF users.name, OUT v_surname TYPE OF users.surname, OUT v_email TYPE OF users_email.email)
BEGIN
    SELECT  u.name, u.surname, ue.email INTO v_name, v_surname, v_email
    FROM    users AS u
            INNER JOIN users_email AS ue ON u.email = ue.id
    WHERE   u.id = v_id;
END//

DELIMITER ;