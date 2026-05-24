package edu.fauser.tpsit.progettotpsit.helper;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import edu.fauser.tpsit.progettotpsit.obj.DBConnection;
import edu.fauser.tpsit.progettotpsit.obj.Fetch;
import edu.fauser.tpsit.progettotpsit.singleton.EnvVar;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class GoogleHelper
{
    private static final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory()).setAudience(Collections.singleton(EnvVar.instance().getEnv().get("WEB_CLIENT_ID"))).build();
    public static long manageGoogleUser(DBConnection con, String credential) throws GeneralSecurityException, IOException, SQLException, ExecutionException, InterruptedException
    {
        GoogleIdToken.Payload payload = checkCredential(credential);
        Optional<Long> id = con.retrieveGoogleUserID(payload.getSubject());
        return (id.isPresent() ? id.get() : insertUser(con, payload));
    }
    private static GoogleIdToken.Payload checkCredential(String credential) throws GeneralSecurityException, IOException
    {
        GoogleIdToken id = verifier.verify(credential);
        if (id == null)
            throw new IllegalArgumentException("Invalid credential");
        return id.getPayload();
    }
    private static long insertUser(DBConnection con, GoogleIdToken.Payload payload) throws ExecutionException, InterruptedException, SQLException
    {
        return con.insertGoogleUser((String) payload.get("given_name"), (String) payload.get("family_name"), fetchPfp((String) payload.get("picture")), payload.getEmail(), payload.getSubject());
    }
    private static InputStream fetchPfp(String url) throws ExecutionException, InterruptedException
    {
        return new ByteArrayInputStream(new Fetch(url).fetchBytes().get().body());
    }
}