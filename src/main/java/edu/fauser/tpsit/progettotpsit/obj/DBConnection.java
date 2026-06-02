package edu.fauser.tpsit.progettotpsit.obj;

import edu.fauser.tpsit.progettotpsit.entity.Comment;
import edu.fauser.tpsit.progettotpsit.entity.POIMeta;
import edu.fauser.tpsit.progettotpsit.entity.User;
import edu.fauser.tpsit.progettotpsit.entity.UserPOIMeta;
import edu.fauser.tpsit.progettotpsit.helper.HashHelper;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

public class DBConnection implements AutoCloseable
{
    public static final int MAX_PFP_SIZE = 65535;
    private static final String DBURL = "jdbc:mysql://" + EnvVar.instance().getEnv().get("DB_HOST", "localhost") + ":3306/db12636";
    private final Connection con;
    public DBConnection() throws SQLException
    {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        con = DriverManager.getConnection(DBURL, EnvVar.instance().getEnv().get("DB_ROOT"), EnvVar.instance().getEnv().get("DB_ROOT_PSW"));
    }
    public Optional<Long> retrieveNormalUserID(String email, String pass) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL GET_NORMAL_USER_INFO(?, ?, ?)}"))
        {
            stmt.setString(1, email);
            stmt.registerOutParameter(2, Types.BINARY);
            stmt.registerOutParameter(3, Types.BIGINT);
            stmt.execute();
            return (stmt.getLong(3) != 0 && HashHelper.checkHash(pass, stmt.getBytes(2)) ? Optional.of(stmt.getLong(3)) : Optional.empty());
        }
    }
    public Optional<Long> retrieveGoogleUserID(String gid) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL GET_GOOGLE_USER_INFO(?, ?)}"))
        {
            stmt.setString(1, gid);
            stmt.registerOutParameter(2, Types.BIGINT);
            stmt.execute();
            return (stmt.getLong(2) == 0 ? Optional.empty() : Optional.of(stmt.getLong(2)));
        }
    }
    public String retrieveUserName(long uid) throws SQLException
    {
        User u = retrieveUser(uid);
        return u.name() + " " + u.surname();
    }
    public User retrieveUser(long uid) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL GET_USER_DISPLAY_NAME(?, ?, ?, ?)}"))
        {
            stmt.setLong(1, uid);
            stmt.registerOutParameter(2, Types.VARCHAR);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
            return new User(stmt.getString(2), stmt.getString(3), stmt.getString(4));
        }
    }
    public String retrieveEmail(long uid) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL GET_USER_MAIL(?, ?)}"))
        {
            stmt.setLong(1, uid);
            stmt.registerOutParameter(2, Types.VARCHAR);
            stmt.execute();
            return stmt.getString(2);
        }
    }
    public String insertNormalUser(String name, String surname, String email, String pass, Part pfp) throws SQLException, IOException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL INSERT_NORMAL_USER(?, ?, ?, ?, ?, ?)}"))
        {
            stmt.setString(1, name);
            stmt.setString(2, surname);
            if (pfp.getSize() == 0)
                stmt.setNull(3, Types.BLOB);
            else
                stmt.setBlob(3, pfp.getInputStream());
            stmt.setString(4, email);
            stmt.setBytes(5, HashHelper.scrypt(pass).getResultAsBytes());
            stmt.registerOutParameter(6, Types.CHAR);
            stmt.execute();
            return stmt.getString(6);
        }
    }
    public void updateUser(long uid, String name, String surname, Part pfp) throws SQLException, IOException
    {
        try (PreparedStatement stmt = con.prepareCall("UPDATE users SET name = " + (name == null ? "name" : "?") + ", surname = " + (surname == null ? "surname" : "?") + ", pfp = " + (pfp.getSize() == 0 ? "pfp" : "?") + " WHERE id = ?"))
        {
            int current = 1;
            if (name != null)
                stmt.setString(current++, name);
            if (surname != null)
                stmt.setString(current++, surname);
            if (pfp.getSize() != 0)
                stmt.setBlob(current++, pfp.getInputStream());
            stmt.setLong(current, uid);
            if (current > 1)
                stmt.execute();
        }
    }
    public long insertGoogleUser(String name, String surname, InputStream pfp, String email, String gid) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL INSERT_GOOGLE_USER(?, ?, ?, ?, ?, ?)}"))
        {
            stmt.setString(1, name);
            stmt.setString(2, surname);
            stmt.setBlob(3, pfp);
            stmt.setString(4, email);
            stmt.setString(5, gid);
            stmt.registerOutParameter(6, Types.BIGINT);
            stmt.execute();
            return stmt.getLong(6);
        }
    }
    public SCVerify verifyUserMail(String code, Timestamp ts) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL VERIFY_USER(?, ?, ?)}"))
        {
            stmt.setString(1, code);
            stmt.setTimestamp(2, ts);
            stmt.registerOutParameter(3, Types.TINYINT);
            stmt.execute();
            return SCVerify.fromSC(stmt.getInt(3));
        }
    }
    public SCVerify verifyUserMail(String code) throws SQLException
    {
        return verifyUserMail(code, Timestamp.from(Instant.now()));
    }
    public String setChangePassword(String email) throws NoSuchAlgorithmException, SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL SET_CHANGE_PASS_CODE(?, ?)}"))
        {
            String code = HashHelper.generateCode(8);
            stmt.setString(1, email);
            stmt.setString(2, code);
            stmt.execute();
            return code;
        }
    }
    public SCVerify changePassword(String email, String code, String pass, Timestamp ts) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL CHANGE_PASS(?, ?, ?, ?, ?)}"))
        {
            stmt.setString(1, email);
            stmt.setTimestamp(2, ts);
            stmt.setString(3, code);
            stmt.setBytes(4, HashHelper.scrypt(pass).getResultAsBytes());
            stmt.registerOutParameter(5, Types.TINYINT);
            stmt.execute();
            return SCVerify.fromSC(stmt.getInt(5));
        }
    }
    public SCVerify changePassword(String email, String code, String pass) throws SQLException
    {
        return changePassword(email, code, pass, Timestamp.from(Instant.now()));
    }
    public Blob getUserPfp(Long uid) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL GET_USER_PFP(?, ?)}"))
        {
            stmt.setLong(1, uid);
            stmt.registerOutParameter(2, Types.BLOB);
            stmt.execute();
            return stmt.getBlob(2);
        }
    }
    private POIMeta getPoiMeta(long osmId, Long ignoreId) throws SQLException
    {
        try (CallableStatement callStmt = con.prepareCall("{CALL GET_POI_META(?, ?)}");
             PreparedStatement prepStmt = con.prepareCall("SELECT u.id, u.name, u.surname, upc.comment, UNIX_TIMESTAMP(upc.when_posted) FROM users_poi_comment AS upc INNER JOIN users AS u ON u.id = upc.user_id WHERE upc.osm_id = ? AND upc.user_id != ? ORDER BY upc.when_posted ASC"))
        {
            callStmt.setLong(1, osmId);
            callStmt.registerOutParameter(2, Types.BIGINT);
            callStmt.execute();
            prepStmt.setLong(1, osmId);
            prepStmt.setLong(2, (ignoreId == null ? 0 : ignoreId));
            return new POIMeta(callStmt.getLong(2), buildCommentsArray(prepStmt.executeQuery()));
        }
    }
    private ArrayList<Comment> buildCommentsArray(ResultSet res) throws SQLException
    {
        ArrayList<Comment> comments = new ArrayList<>();
        while (res.next())
            comments.add(new Comment(res.getLong(1), res.getString(2) + " " + res.getString(3), res.getString(4), res.getInt(5)));
        return comments;
    }
    private ArrayList<Comment> buildCommentsArray(ResultSet res, long fixedId) throws SQLException
    {
        ArrayList<Comment> comments = new ArrayList<>();
        while (res.next())
            comments.add(new Comment(res.getLong(5), fixedId, res.getString(1) + " " + res.getString(2), res.getString(3), res.getInt(4)));
        return comments;
    }
    public POIMeta getPoiMeta(long osmId) throws SQLException
    {
        return getPoiMeta(osmId, null);
    }
    public UserPOIMeta getUserPoiMeta(long osmId, long userId) throws SQLException
    {
        try (CallableStatement callStmt = con.prepareCall("{CALL GET_USER_POI_META(?, ?, ?, ?, ?)}");
             PreparedStatement prepStmt = con.prepareCall("SELECT u.name, u.surname, upc.comment, UNIX_TIMESTAMP(upc.when_posted), upc.id FROM users_poi_comment AS upc INNER JOIN users AS u ON u.id = upc.user_id WHERE upc.osm_id = ? AND upc.user_id = ? ORDER BY upc.when_posted ASC"))
        {
            callStmt.setLong(1, userId);
            callStmt.setLong(2, osmId);
            callStmt.registerOutParameter(3, Types.BOOLEAN);
            callStmt.registerOutParameter(4, Types.BOOLEAN);
            callStmt.registerOutParameter(5, Types.BOOLEAN);
            callStmt.execute();
            prepStmt.setLong(1, osmId);
            prepStmt.setLong(2, userId);
            return new UserPOIMeta(callStmt.getBoolean(3), callStmt.getBoolean(4), callStmt.getBoolean(5), buildCommentsArray(prepStmt.executeQuery(), userId), getPoiMeta(osmId, userId));
        }
    }
    public void changeUserPoiRel(long osmId, long userId, boolean doesLike, boolean favourite) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL CHANGE_USER_POI_REL(?, ?, ?, ?)}"))
        {
            stmt.setLong(1, osmId);
            stmt.setLong(2, userId);
            stmt.setBoolean(3, doesLike);
            stmt.setBoolean(4, favourite);
            stmt.execute();
        }
    }
    public long postComment(long osmId, long userId, String comment) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL POST_COMMENT(?, ?, ?, ?)}"))
        {
            stmt.setLong(1, osmId);
            stmt.setLong(2, userId);
            stmt.setString(3, comment);
            stmt.registerOutParameter(4, Types.BIGINT);
            stmt.execute();
            return stmt.getLong(4);
        }
    }
    public long reservePoi(long osmId, long userId, Timestamp start, Timestamp end) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL RESERVE_POI(?, ?, ?, ?, ?)}"))
        {
            stmt.setLong(1, osmId);
            stmt.setLong(2, userId);
            stmt.setTimestamp(3, start);
            stmt.setTimestamp(4, end);
            stmt.registerOutParameter(5, Types.BIGINT);
            stmt.execute();
            return stmt.getLong(5);
        }
    }
    public void deleteReservation(long id, long uid) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL DELETE_RESERVATION(?, ?)}"))
        {
            stmt.setLong(1, id);
            stmt.setLong(2, uid);
            stmt.execute();
        }
    }
    public void deleteComment(long id, long uid) throws SQLException
    {
        try(CallableStatement stmt = con.prepareCall("{CALL DELETE_COMMENT(?, ?)}"))
        {
            stmt.setLong(1, id);
            stmt.setLong(2, uid);
            stmt.execute();
        }
    }
    public ArrayList<Long> getUserSaved(long userId) throws SQLException
    {
        try (PreparedStatement stmt = con.prepareCall("SELECT up.osm_id FROM users_poi AS up WHERE up.user_id = ? AND up.favourite = TRUE"))
        {
            stmt.setLong(1, userId);
            return buildIdsArray(stmt.executeQuery());
        }
    }
    private ArrayList<Long> buildIdsArray(ResultSet res) throws SQLException
    {
        ArrayList<Long> arr = new ArrayList<>();
        while (res.next())
            arr.add(res.getLong(1));
        return arr;
    }
    @Override
    public void close() throws SQLException
    {
        con.close();
    }
    public enum SCVerify
    {
        Found(1),
        NotFound(0),
        FoundButExpired(-1);
        private final int code;
        SCVerify(int code)
        {
            this.code = code;
        }
        public int getCode()
        {
            return code;
        }
        public static SCVerify fromSC(int code)
        {
            for (SCVerify sc : values())
                if (sc.code == code)
                    return sc;
            throw new IllegalArgumentException("Invalid code passed");
        }
    }
}