package edu.fauser.tpsit.progettotpsit.obj;

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
    public Optional<String> retrieveUserName(Long uid) throws SQLException
    {
        try (ResultSet res = con.createStatement().executeQuery("SELECT u.name, u.surname " +
                                                                "FROM users AS u " +
                                                                "WHERE u.id = " + uid))
        {
            return (res.next() ? Optional.of(res.getString("name") + " " + res.getString("surname")) : Optional.empty());
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
        try (CallableStatement stmt = con.prepareCall("{CALL GET_POI_META(?, ?)}");
             ResultSet res = con.createStatement().executeQuery("{SELECT up.comment FROM users_poi WHERE up.comment IS NOT NULL " + (ignoreId == null ? "" : "AND up.user_id != " + ignoreId) +"}"))
        {
            stmt.setLong(1, osmId);
            stmt.registerOutParameter(2, Types.BIGINT);
            stmt.execute();
            return new POIMeta(osmId, stmt.getLong(2), buildCommentsArray(res));
        }
    }
    private ArrayList<String> buildCommentsArray(ResultSet res) throws SQLException
    {
        ArrayList<String> comments = new ArrayList<>();
        while (res.next())
            comments.add(res.getString(1));
        return comments;
    }
    public POIMeta getPoiMeta(long osmId) throws SQLException
    {
        return getPoiMeta(osmId, null);
    }
    public UserPOIMeta getUserPoiMeta(long osmId, long userId) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL GET_USER_POI_META(?, ?, ?, ?, ?)}"))
        {
            stmt.setLong(1, userId);
            stmt.setLong(2, osmId);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.registerOutParameter(4, Types.BOOLEAN);
            stmt.registerOutParameter(5, Types.BOOLEAN);
            stmt.execute();
            return new UserPOIMeta(userId, stmt.getBoolean(4), stmt.getBoolean(5), stmt.getString(3), getPoiMeta(osmId, userId));
        }
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