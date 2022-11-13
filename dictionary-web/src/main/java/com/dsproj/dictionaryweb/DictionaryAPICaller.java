/**
 * Author: Jiahe Tian
 * Last Modified: Nov 13, 2022
 *
 * DictionaryAPICaller is an encapsulation class of the third-party API serving as the data source.
 * QueryResult is the container class for the search result, and it will be returned to DictionaryServlet.
 */
package com.dsproj.dictionaryweb;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

class QueryResult {
    public String origin;
    public String formatted;

    public QueryResult(String origin, String formatted) {
        this.origin = origin;
        this.formatted = formatted;
    }
}

public class DictionaryAPICaller {

    private OkHttpClient httpClient;
    private String owlbotToken;

    /**
     * The basic constructor of DictionaryAPICaller. It will initialize an OkHttpClient and read the third-party API
     * token from the system environmental variables for safety purpose.
     */
    public DictionaryAPICaller() {
        httpClient = new OkHttpClient();
        owlbotToken = System.getenv("OWLBOT_TOKEN");
    }

    /**
     * This method handles the third-party API calls. The url for the third-party API is defined in the parameter as
     * url. After receiving the result, the method will create a QueryResult to hold the result and return it. The query
     * result will store the JSON string for both origin and formatted.
     *
     * @param url the url of the third-party API
     * @return QueryResult that contains the result
     */
    public QueryResult sendGetURL(String url) {
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", owlbotToken)  // add request headers
                .build();
        String json = null;
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            json = response.body().string();
            System.out.println(json);
            JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

            // get(0): always get the first definition and ignore others
            JsonObject definitionJsonObj = jsonObject.get("definitions").getAsJsonArray().get(0).getAsJsonObject();
            String definition = "";
            if (definitionJsonObj.has("definition") && !definitionJsonObj.get("definition").isJsonNull()) {
                definition = definitionJsonObj.get("definition").getAsString();
            }
            String imageUrl = "";
            if (definitionJsonObj.has("image_url") && !definitionJsonObj.get("image_url").isJsonNull()) {
                imageUrl = definitionJsonObj.get("image_url").getAsString();
            }

            // prepare for the formatted
            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("definition", definition);
            responseObj.addProperty("image_url", imageUrl);
            return new QueryResult(json, responseObj.toString());
        } catch (com.google.gson.JsonSyntaxException e) {
            e.printStackTrace();
        } catch (java.net.UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // return the empty JSON strings by default
        return new QueryResult(new JsonObject().toString(), new JsonObject().toString());
    }

    /**
     * The method will get the input and forward the input to sendGetURL(String url).
     *
     * @param input the input typed in the search box on an Android device
     * @return QueryResult that contains the result
     */
    public QueryResult sendGet(String input) {
        try {
            String url = "https://owlbot.info/api/v4/dictionary/" + URLEncoder.encode(input, "UTF-8");
            return sendGetURL(url);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * The test funcion.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        DictionaryAPICaller obj = new DictionaryAPICaller();
        QueryResult res = obj.sendGet("apple and banana");
        System.out.println(res.formatted);
        System.out.println("ok");
    }

}