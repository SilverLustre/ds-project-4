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
import static com.mongodb.client.model.Filters.eq;
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
    MongoDatabase database;

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

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        List<Document> aggregatedLog = getAggregatedLog(20);
        List<Document> topModel = getTopModel(5);
        List<Document> topInput = getTopInput(10);
        double durationAvg = getDurationAvg();
        request.setAttribute("aggregatedLog", aggregatedLog);
        request.setAttribute("topModel", topModel);
        request.setAttribute("topInput", topInput);
        request.setAttribute("durationAvg", durationAvg);
        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }

    private List<Document> getAggregatedLog() {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        List<Document> res = log.find().sort(new Document().append("start_time", -1)).into(new ArrayList<>());
        return res;
    }

    private List<Document> getAggregatedLog(int top) {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        List<Document> res = log.find().sort(new Document().append("start_time", -1)).limit(top).into(new ArrayList<>());
        return res;
    }

    private List<Document> getTopModel(int top) {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        Bson group = group("$model", sum("model_count", 1));
        Bson sort = sort(descending("model_count"));
        Bson limit = limit(top);
        List<Document> res = log.aggregate(Arrays.asList(group, sort, limit)).into(new ArrayList<>());
        return res;
    }

    private List<Document> getTopInput(int top) {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        Bson group = group("$input", sum("input_count", 1));
        Bson sort = sort(descending("input_count"));
        Bson limit = limit(top);
        List<Document> res = log.aggregate(Arrays.asList(group, sort, limit)).into(new ArrayList<>());
        return res;
    }

    private double getDurationAvg() {
        MongoCollection<Document> log = database.getCollection("aggregated_log");
        Bson group = group(null, avg("duration_avg", "$duration"));
        List<Document> res = log.aggregate(Arrays.asList(group)).into(new ArrayList<>());
        System.out.println(res);
        double durationAvg = res.get(0).getDouble("duration_avg");
        System.out.println("durationAvg:" + durationAvg);
        return durationAvg;
    }

    public void destroy() {
    }
}