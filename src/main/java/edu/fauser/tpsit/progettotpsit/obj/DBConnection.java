package edu.fauser.tpsit.progettotpsit.obj;

import com.password4j.Hash;
import edu.fauser.tpsit.progettotpsit.helper.HashHelper;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import java.io.InputStream;
import java.math.BigInteger;
import java.sql.*;
import java.util.Optional;

public class DBConnection implements AutoCloseable
{
    private final Connection con;
    public DBConnection() throws SQLException
    {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        con = DriverManager.getConnection(getDBURL(), EnvVar.getInstance().getEnv().get("DB_ROOT"), EnvVar.getInstance().getEnv().get("DB_ROOT_PSW"));
    }
    private static String getDBURL()
    {
        return "jdbc:mysql://" + EnvVar.getInstance().getEnv().get("DB_HOST", "localhost") + ":3306/db12636";
    }
    public Optional<Long> retrieveUserID(String email, String pass) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{? = CALL get_user_info(?, ?)}"))
        {
            stmt.registerOutParameter(1, Types.BIGINT);
            stmt.registerOutParameter("@pass", Types.BINARY);
            stmt.setString("@email", email);
            stmt.execute();
            return (HashHelper.checkHash(pass, stmt.getBytes("@pass")) ? Optional.of(stmt.getLong(1)) : Optional.empty());
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
    public void insertUser(String name, String surname, String email, String pass, InputStream pfp) throws SQLException
    {
        try (CallableStatement stmt = con.prepareCall("{CALL insert_user(?, ?, ?, ?, ?)}"))
        {
            stmt.setString("@name", name);
            stmt.setString("@surname", surname);
            stmt.setString("@email", email);
            stmt.setBytes("@pass", HashHelper.scrypt(pass).getBytes());
            stmt.setBlob("@pfp", pfp);
            stmt.execute();
        }
    }
    @Override
    public void close() throws SQLException
    {
        con.close();
    }
}