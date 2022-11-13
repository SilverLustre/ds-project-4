/**
 * Author: Jiahe Tian
 * Last Modified: Nov 13, 2022
 *
 * This the servlet for the dashboard function of the dictionary service. The service receive requests from desktop
 * browsers, query the mongodb database, and return the formatted result.
 */
package com.dsproj.dictionaryweb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Sorts.descending;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.IOException;


@WebServlet(name = "dashboardServlet", value = "/dashboard")
public class DashboardServlet extends HttpServlet {
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
     * The method will be called when /dashboard route receives an HTTP GET request, and it will query the MongoDB for
     * the log and statistics data, and forward the data to dashboard.jsp page. The jsp page will be rendered with the
     * data and return the page to the client.
     *
     * @param request incoming HTTP request
     * @param response outcoming HTTP response
     * @throws IOException
     * @throws ServletException
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<Document> aggregatedLog = getAggregatedLog();
        List<Document> topModel = getTopModel(5);
        List<Document> topInput = getTopInput(10);
        double durationAvg = getDurationAvg();
        request.setAttribute("aggregatedLog", aggregatedLog);
        request.setAttribute("topModel", topModel);
        request.setAttribute("topInput", topInput);
        request.setAttribute("durationAvg", durationAvg);
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }

    /**
     * Query all the log in the MongoDB, and sort the result by start_time descending.
     *
     * @return all the log in a List of Document.
     */
    private List<Document> getAggregatedLog() {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        List<Document> res = log.find().sort(new Document().append("start_time", -1)).into(new ArrayList<>());
        return res;
    }

    /**
     * Query all the log in the MongoDB, sort the result by start_time descending, and only keep the top number of
     * records.
     *
     * @param top the top number of records.
     * @return the top log in a List of Document.
     */
    private List<Document> getAggregatedLog(int top) {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        List<Document> res = log.find().sort(new Document().append("start_time", -1)).limit(top).into(new ArrayList<>());
        return res;
    }

    /**
     * Query the top mobile device model.
     *
     * @param top the top number of models.
     * @return the top models in a List of Document.
     */
    private List<Document> getTopModel(int top) {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        Bson group = group("$model", sum("model_count", 1));
        Bson sort = sort(descending("model_count"));
        Bson limit = limit(top);
        List<Document> res = log.aggregate(Arrays.asList(group, sort, limit)).into(new ArrayList<>());
        return res;
    }

    /**
     * Query the top input.
     *
     * @param top the top number of inputs.
     * @return the top inputs in a List of Document.
     */
    private List<Document> getTopInput(int top) {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        Bson group = group("$input", sum("input_count", 1));
        Bson sort = sort(descending("input_count"));
        Bson limit = limit(top);
        List<Document> res = log.aggregate(Arrays.asList(group, sort, limit)).into(new ArrayList<>());
        return res;
    }

    /**
     * Query the average duration of all requests in the MongoDB collection.
     *
     * @return the average duration of requests.
     */
    private double getDurationAvg() {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        Bson group = group(null, avg("duration_avg", "$duration"));
        List<Document> res = log.aggregate(Arrays.asList(group)).into(new ArrayList<>());
        System.out.println(res);
        double durationAvg = 0;
        if (res.size() > 0) {
            durationAvg = res.get(0).getDouble("duration_avg");
        }
        System.out.println("durationAvg:" + durationAvg);
        return durationAvg;
    }

    /**
     * The function will be called when the servlet is being destroyed.
     */
    public void destroy() {
    }
}