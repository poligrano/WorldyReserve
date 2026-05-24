package edu.fauser.tpsit.progettotpsit.singleton;

import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

public class EnvVar
{
    private static EnvVar instance;
    private final Dotenv env;
    private EnvVar()
    {
        env = Dotenv.configure().directory(Objects.requireNonNull(getClass().getClassLoader().getResource("/env")).getPath()).load();
    }
    public static EnvVar instance()
    {
        return instance == null ? (instance = new EnvVar()) : instance;
    }
    public Dotenv getEnv()
    {
        return env;
    }
}