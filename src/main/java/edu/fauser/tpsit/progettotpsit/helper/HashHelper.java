package edu.fauser.tpsit.progettotpsit.helper;

import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.ScryptFunction;

public class HashHelper
{
    private static final ScryptFunction sc = ScryptFunction.getInstance(65536, 8, 2, 16);
    public static Hash scrypt(String password)
    {
        return Password.hash(password.getBytes()).addRandomSalt(16).with(sc);
    }
    public static boolean checkHash(String password, byte[] hash)
    {
        return Password.check(password.getBytes(), hash).with(sc);
    }
}