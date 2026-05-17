package edu.fauser.tpsit.progettotpsit.obj;

import edu.fauser.tpsit.progettotpsit.helper.HashHelper;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import javax.servlet.http.Part;
import java.io.IOException;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class DBConnection implements AutoCloseable
{
    private static final String DBURL = "jdbc:mysql://" + EnvVar.getInstance().getEnv().get("DB_HOST", "localhost") + ":3306/db12636";
    private final Connection con;
    public DBConnection() throws SQLException
    {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        con = DriverManager.getConnection(DBURL, EnvVar.getInstance().getEnv().get("DB_ROOT"), EnvVar.getInstance().getEnv().get("DB_ROOT_PSW"));
    }
    public Optional<Long> retrieveNormalUserID(String email, String pass) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL GET_NORMAL_USER_INFO(?, ?, ?)}"))
        {
            stmt.registerOutParameter("v_id", Types.BIGINT);
            stmt.registerOutParameter("v_pass", Types.BINARY);
            stmt.setString("v_email", email);
            stmt.execute();
            return (stmt.getLong("v_id") != 0 && HashHelper.checkHash(pass, stmt.getBytes("v_pass")) ? Optional.of(stmt.getLong("v_id")) : Optional.empty());
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
        try (CallableStatement stmt = con.prepareCall("{CALL insert_user(?, ?, ?, ?, ?, ?)}"))
        {
            stmt.registerOutParameter("v_code", Types.CHAR);
            stmt.setString("v_name", name);
            stmt.setString("v_surname", surname);
            stmt.setString("v_email", email);
            stmt.setBytes("v_pass", HashHelper.scrypt(pass).getBytes());
            stmt.setBlob("v_pfp", (pfp == null ? null : pfp.getInputStream()));
            stmt.execute();
            return stmt.getString("v_code");
        }
    }
    public SCVerify verifyUserMail(String code, Timestamp ts) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL VERIFY_USER(?, ?, ?)}"))
        {
            stmt.registerOutParameter("v_sc", Types.TINYINT);
            stmt.setString("v_code", code);
            stmt.setTimestamp("v_now", ts);
            stmt.execute();
            return SCVerify.fromSC(stmt.getInt("v_sc"));
        }
    }
    public SCVerify verifyUserMail(String code) throws SQLException
    {
        return verifyUserMail(code, Timestamp.from(Instant.now()));
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