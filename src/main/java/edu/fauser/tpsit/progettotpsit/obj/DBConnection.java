package edu.fauser.tpsit.progettotpsit.obj;

import edu.fauser.tpsit.progettotpsit.helper.HashHelper;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import javax.servlet.http.Part;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
            stmt.setString(1, email);
            stmt.registerOutParameter(2, Types.BINARY);
            stmt.registerOutParameter(3, Types.BIGINT);
            stmt.execute();
            return (stmt.getLong(3) != 0 && HashHelper.checkHash(pass, stmt.getBytes(2)) ? Optional.of(stmt.getLong(3)) : Optional.empty());
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
            stmt.setBlob(3, (pfp == null ? null : pfp.getInputStream()));
            stmt.setString(4, email);
            stmt.setBytes(5, HashHelper.scrypt(pass).getResultAsBytes());
            stmt.registerOutParameter(6, Types.CHAR);
            stmt.execute();
            return stmt.getString(6);
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