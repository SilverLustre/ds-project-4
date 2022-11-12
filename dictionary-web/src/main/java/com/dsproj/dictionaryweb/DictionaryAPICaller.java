package com.dsproj.dictionaryweb;//package com.mkyong.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jndi.toolkit.url.UrlUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class DictionaryAPICaller {

    // only one client, singleton, better puts it in a factory,
    // multiple instances will create more memory.
    private OkHttpClient httpClient;

    public DictionaryAPICaller() {
        httpClient = new OkHttpClient();
    }

    public String sendGETSync(String input) {
        try {
            String url = "https://owlbot.info/api/v4/dictionary/" + URLEncoder.encode(input, "UTF-8");
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Token 4cc71b234ded51f7265672fc7060445954125471")  // add request headers
                    .build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String json = response.body().string();
                System.out.println(json);
                JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
                JsonObject definitionJsonObj = jsonObject.get("definitions").getAsJsonArray().get(0).getAsJsonObject();
                String definition = "";
                if (definitionJsonObj.has("definition")&&!definitionJsonObj.get("definition").isJsonNull()) {
                    definition = definitionJsonObj.get("definition").getAsString();
                }
                String imageUrl = "";
                if (definitionJsonObj.has("image_url")&&!definitionJsonObj.get("image_url").isJsonNull()) {
                    imageUrl = definitionJsonObj.get("image_url").getAsString();
                }

                JsonObject responseObj = new JsonObject();
                responseObj.addProperty("definition", definition);
                responseObj.addProperty("image_url", imageUrl);
                return responseObj.toString();
            } catch (java.net.UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return new JsonObject().toString();
    }

    public static void main(String[] args) throws IOException {
        DictionaryAPICaller obj = new DictionaryAPICaller();
        String res = obj.sendGETSync("apple and banana");
        System.out.println(res);
        System.out.println("ok");
    }

}