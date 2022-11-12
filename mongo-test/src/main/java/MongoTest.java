import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;

public class MongoTest {
    public static void main(String[] args) {
        String mongoPass = System.getenv("MONGO_PASS");
        ConnectionString connectionString = new ConnectionString(String.format("mongodb+srv://owlat:%s@cluster0.lylnbco.mongodb.net/?retryWrites=true&w=majority", mongoPass));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("owlat");
        MongoCollection<Document> stringStore = database.getCollection("string_store");

        Scanner in = new Scanner(System.in);
        try {
            while (true) {
                System.out.println("Select options:");
                System.out.println("1. Add a string to the MongoDB collection");
                System.out.println("2. Print all strings in the MongoDB collection");
                System.out.println("3. Close the connection and exit");
                int select = -1;
                try {
                    select = in.nextInt();
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input, please try again.");
                    in.nextLine();
                    continue;
                }

                if (select == 1) {
                    System.out.println("Please input the string to be added:");
                    in.nextLine();
                    String input = in.nextLine();
                    stringStore.insertOne(new Document().append("string", input));
                } else if (select == 2) {
                    Iterator<Document> it = stringStore.find().iterator();
                    while (it.hasNext()) {
                        Document curDoc = it.next();
                        String id = (String) curDoc.get("_id").toString();
                        String string = (String) curDoc.get("string");
                        System.out.println(String.format("id:%s, string:%s", id, string));
                    }
                } else if (select == 3) {
                    break;
                } else {
                    System.err.println("Invalid input, please try again.");
                }
            }
        } finally {
            System.out.println("Closing the connection");
            mongoClient.close();
            System.out.println("Closed.");
        }
    }
}
