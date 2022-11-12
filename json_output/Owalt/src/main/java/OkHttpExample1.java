//package com.mkyong.http;

import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;

class Definition{
    String type;
    String definition;
    String example;
    String image_url;
    String emoji;

    @Override
    public String toString() {
        return  "\n"+"{"+"\n"+
                "type='" + type + '\'' +"\n"+
                "definition='" + definition + '\'' +"\n"+
                "example='" + example + '\'' +"\n"+
                "image_url='" + image_url + '\'' +"\n"+
                "emoji='" + emoji + '\''+"\n"+
                "}"+"\n";
    }
}
class Body{
    ArrayList<Definition>definitions = new ArrayList<>();
    String word;
    String pronunciation;

    @Override
    public String toString() {
        return "Body{" +"\n"+
                "definitions=" + definitions + "\n"+
                "word='" + word + '\'' +"\n"+
                "pronunciation='" + pronunciation + '\'' +"\n"+
                '}';
    }
}

public class OkHttpExample1 {

    // only one client, singleton, better puts it in a factory,
    // multiple instances will create more memory.
    private final OkHttpClient httpClient = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        OkHttpExample1 obj = new OkHttpExample1();
        obj.sendGETSync();
    }

    private void sendGETSync() throws IOException {

        Request request = new Request.Builder()
                .url("https://owlbot.info/api/v4/dictionary/apple")
                .addHeader("Authorization", "Token 4cc71b234ded51f7265672fc7060445954125471")  // add request headers
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

//            // Get response headers
//            Headers responseHeaders = response.headers();
//            for (int i = 0; i < responseHeaders.size(); i++) {
//                System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//            }

            // Get response body
            String body = response.body().string();
            Gson gson = new Gson();
            Body res = gson.fromJson(body,Body.class);
            System.out.println(res.toString());
        }

    }

}