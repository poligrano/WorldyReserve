package edu.fauser.tpsit.progettotpsit.obj;

import com.password4j.Hash;
import edu.fauser.tpsit.progettotpsit.helper.HashHelper;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.*;
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
    public void insertNormalUser(String name, String surname, String email, String pass, Part pfp) throws SQLException, IOException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL insert_user(?, ?, ?, ?, ?)}"))
        {
            stmt.setString("v_name", name);
            stmt.setString("v_surname", surname);
            stmt.setString("v_email", email);
            stmt.setBytes("v_pass", HashHelper.scrypt(pass).getBytes());
            stmt.setBlob("v_pfp", (pfp == null ? null : pfp.getInputStream()));
            stmt.execute();
        }
    }
    @Override
    public void close() throws SQLException
    {
        con.close();
    }
}