package edu.fauser.tpsit.progettotpsit.helper;

import com.password4j.Hash;
import com.password4j.Password;
import com.password4j.ScryptFunction;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
    public static String generateCode(int n) throws NoSuchAlgorithmException
    {
        if (n <= 0)
            throw new IllegalArgumentException("Number of digits must be greater than 0");
        int lb = (int) Math.pow(10, n - 1);
        return String.valueOf(SecureRandom.getInstanceStrong().nextInt(Integer.parseInt("9".repeat(n)) - lb) + lb);
    }
}