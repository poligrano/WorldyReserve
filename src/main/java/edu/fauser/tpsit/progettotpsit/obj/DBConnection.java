package edu.fauser.tpsit.progettotpsit.obj;

import edu.fauser.tpsit.progettotpsit.helper.HashHelper;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

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
        try (ResultSet res = con.createStatement().executeQuery("SELECT u.id, up.pass " +
                                                                "FROM users AS u " +
                                                                "INNER JOIN users_pass AS up ON u.pass = up.id " +
                                                                "INNER JOIN users_email AS ue ON u.email = ue.id " +
                                                                "WHERE ue.email = '" + email + "'"))
        {
            return (res.next() && HashHelper.checkHash(pass, res.getBytes("pass")) ? Optional.of(res.getLong("uid")) : Optional.empty());
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
    @Override
    public void close() throws SQLException
    {
        con.close();
    }
}