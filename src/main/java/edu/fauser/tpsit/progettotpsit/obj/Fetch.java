package edu.fauser.tpsit.progettotpsit.obj;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Fetch
{
    private final URI uri;
    private final HttpClient client;
    public Fetch(String uri)
    {
        client = HttpClient.newHttpClient();
        this.uri = URI.create(uri);
    }
    public CompletableFuture<HttpResponse<byte[]>> fetchBytes()
    {
        return client.sendAsync(HttpRequest.newBuilder(uri).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
    }
}