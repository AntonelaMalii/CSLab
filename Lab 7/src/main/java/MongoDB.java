import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Iterator;

public class MongoDB {
    public static String[] documents = new String[10];
    public static String[] name = new String[10];
    public static String[][] data = new String[10][10];

    public static void main(String[] args) {

        String uri = "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient mongoClient = new MongoClient(clientURI);

        MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");
        MongoCollection<Document> collection = mongoDatabase.getCollection("Data");
        FindIterable<Document> iterDoc = collection.find();
        Iterator it = iterDoc.iterator();
        int i = 0;
        while (it.hasNext()) {
            documents[i] = it.next().toString();
            i++;
        }
        insert_data();
    }

    public static void insert_data(){
        int i = 0;
        int row = 0;
        while (documents[i]!=null) {
            int iname = 0;
            while (!documents[i].startsWith("id")) {
                documents[i] = documents[i].substring(1);
            }
            while (documents[i].length()>0) {
                String temp = "";
                while (!documents[i].startsWith("=")) {
                    temp += documents[i].charAt(0);
                    documents[i] = documents[i].substring(1);
                }
                while (name[iname] != null) {
                    if (name[iname].equals(temp))
                        break;
                    iname++;
                }
                name[iname] = temp;
                temp = "";
                documents[i] = documents[i].substring(1);
                while (!documents[i].startsWith(",") && !documents[i].startsWith("}")) {
                    temp += documents[i].charAt(0);
                    documents[i] = documents[i].substring(1);
                }
                data[row][iname] = temp;
                documents[i] = documents[i].substring(2);
            }
            i++;
            row++;
        }
        print_data();
    }

    public static void print_data(){
        int i = 0;
        while (name[i]!=null){
            System.out.print(name[i] + " | ");
            i++;
        }
        System.out.println();
        i=0;
        while (i<data.length && data[i]!=null){
            int j = 0;
            while (data[i][j]!=null){
                System.out.print(data[i][j] + " | ");
                j++;
            }
            System.out.println();
            i++;
        }
    }
}
