/**
 * Author: Jiahe Tian
 * Last Modified: Nov 13, 2022
 * <p>
 * This the servlet for the Android frontend requests. The service queries the third-party API, and return the JSON
 * result to Android clients.
 */
package com.dsproj.dictionaryweb;

import com.google.gson.JsonObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Date;

@WebServlet(name = "dictionaryServlet", value = "/dictionary")
public class DictionaryServlet extends HttpServlet {
    private MongoDatabase database;

    /**
     * Initialize the database connection.
     */
    public void init() {
        String mongoPass = System.getenv("MONGO_PASS");
        ConnectionString connectionString = new ConnectionString(String.format("mongodb+srv://owlat:%s@cluster0.lylnbco.mongodb.net/?retryWrites=true&w=majority", mongoPass));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("owlat");
    }

    /**
     * The method will be called when /dictionary route receives an HTTP GET request, and it will check the input, call
     * the DictionaryAPICaller to get the API result, log the actions to the MongoDB, and return the JSON string result
     * to the Android client.
     *
     * @param request incoming HTTP request
     * @param response outcoming HTTP response
     * @throws IOException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        // get the log collection from MongoDB
        MongoCollection<Document> log = database.getCollection("aggregated_log");

        String input = request.getParameter("input");
        System.out.println("Get input:" + input);

        String model = request.getParameter("model");
        System.out.println("Get model:" + model);

        response.setContentType("application/json");

        // record the start time
        Date preDT = new Date();

        if (input == null) {
            // the input cannot be forwarded to the third-party API
            // log the actions
            log.insertOne(new Document()
                    .append("input", input)
                    .append("model", model)
                    .append("start_time", preDT)
                    .append("request_from_user", new Document()
                            .append("input", input)
                            .append("model", model))
                    .append("request_to_api", new Document())
                    .append("response_from_api", new Document())
                    .append("response_to_user", new Document())
            );

            // return the JSON result
            out.println(new JsonObject().toString());
        } else {
            // the input is valid, then continue processing
            DictionaryAPICaller dictionaryAPICaller = new DictionaryAPICaller();
            String url = "https://owlbot.info/api/v4/dictionary/" + URLEncoder.encode(input, "UTF-8");
            QueryResult queryResult = dictionaryAPICaller.sendGetURL(url);

            // record the end time
            Date postDT = new Date();

            // log the actions
            log.insertOne(new Document()
                    .append("input", input)
                    .append("model", model)
                    .append("start_time", preDT)
                    .append("end_time", postDT)
                    .append("duration", (postDT.getTime() - preDT.getTime()) / 1000.0)
                    .append("request_from_user", new Document()
                            .append("input", input)
                            .append("model", model))
                    .append("request_to_api", new Document()
                            .append("get_url", url))
                    .append("response_from_api", Document.parse(queryResult.origin))
                    .append("response_to_user", Document.parse(queryResult.formatted))
            );

            // return the JSON result
            out.println(queryResult.formatted);
        }

    }

    /**
     * The function will be called when the servlet is being destroyed.
     */
    public void destroy() {
    }
}