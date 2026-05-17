package edu.fauser.tpsit.progettotpsit.singleton;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvVar
{
    private static EnvVar instance;
    private final Dotenv env;
    private EnvVar()
    {
        env = Dotenv.configure().directory(getClass().getClassLoader().getResource("/env").getPath()).load();
    }
    public static EnvVar getInstance()
    {
        return instance == null ? (instance = new EnvVar()) : instance;
    }
    public Dotenv getEnv()
    {
        return env;
    }
}