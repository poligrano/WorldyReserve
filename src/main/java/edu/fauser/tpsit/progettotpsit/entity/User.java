package edu.fauser.tpsit.progettotpsit.entity;

public class User
{
    private final String name;
    private final String surname;
    private final String email;
    public User(String name, String surname, String email)
    {
        this.name = name;
        this.surname = surname;
        this.email = email;
    }
    public String name()
    {
        return name;
    }
    public String surname()
    {
        return surname;
    }
    public String email()
    {
        return email;
    }
}