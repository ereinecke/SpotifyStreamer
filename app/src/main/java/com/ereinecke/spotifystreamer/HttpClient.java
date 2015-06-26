package com.ereinecke.spotifystreamer;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Client for making http requests, used for debugging
 */
public class HttpClient {

    /* No access token */
    String run(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.d("HttpClient", "request: " + request.toString());
        Response response = client.newCall(request).execute();
        Log.d("HttpClient", "response: " + response.body().string());
        return response.body().string();
    }

    /* uses Access token */
    String run(String url, String accessToken) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", accessToken)
                .build();
        Log.d("HttpClient", "request: " + request.toString());
        Response response = client.newCall(request).execute();
        Log.d("HttpClient", "response: " + response.body().string());
        return response.body().string();
    }


    public static void main(String[] args) throws IOException {
        HttpClient example = new HttpClient();
        String response = example.run("https://raw.github.com/square/okhttp/master/README.md");
        System.out.println(response);
    }
}