import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClientURI;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import org.bson.Document;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Main {
    public static MongoCollection<Document> collection;
    public static AES aes = new AES();
    public static String[] documents = new String[10];
    public static String[] name = new String[10];
    public static String[][] data = new String[10][10];


    public static void main(String[] args) throws InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
        String uri = "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        MongoClientURI clientURI = new MongoClientURI(uri);
        MongoClient mongoClient = new MongoClient(clientURI);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("admin");
        collection = mongoDatabase.getCollection("NewData");
        FindIterable<Document> iterDoc = collection.find();
        Iterator it = iterDoc.iterator();
        insert_data();
        for(int i = 0; it.hasNext(); ++i) {
            documents[i] = it.next().toString();
        }
        get_data(false);
        get_data(true);
    }

    public static void insert_data() throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        String data[][] = new String[3][2];
        data[0][0] = "name";
        data[0][1] = "Kate";
        data[1][0] = "email";
        String tmp = aes.encrypt("kate@gmail.com");
        System.out.println(tmp);
        data[1][1] = tmp.substring(0,tmp.length()-2);
        System.out.println(aes.decrypt(data[1][1] + "=="));
        data[2][0] = "phone";
        tmp = aes.encrypt("+373568368");
        data[2][1] = tmp.substring(0,tmp.length()-2);
        DBObject object = new BasicDBObject(data[0][0],data[0][1]).append(data[1][0], data[1][1]).append(data[2][0], data[2][1]);
        collection.insertOne(new Document(object.toMap()));
        data[0][1] = "Jonny";
        tmp = aes.encrypt("jhon@gmail.com");
        data[1][1] = tmp.substring(0,tmp.length()-2);
        tmp = aes.encrypt("+373569835");
        data[2][1] = tmp.substring(0,tmp.length()-2);
        object = new BasicDBObject(data[0][0],data[0][1]).append(data[1][0], data[1][1]).append(data[2][0], data[2][1]);
        collection.insertOne(new Document(object.toMap()));
        data[0][1] = "Eric";
        tmp = aes.encrypt("eric@gmail.com");
        data[1][1] = tmp.substring(0,tmp.length()-2);
        tmp = aes.encrypt("+373685343");
        data[2][1] = tmp.substring(0,tmp.length()-2);
        object = new BasicDBObject(data[0][0],data[0][1]).append(data[1][0], data[1][1]).append(data[2][0], data[2][1]);

    }

    public static void get_data(boolean decrypt) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        if(decrypt){
            int i = 0;
            while (data[i][0]!=null){
                data[i][2] = aes.decrypt(data[i][2] + "==");
                data[i][3] = aes.decrypt(data[i][3] + "==");
                i++;
            }
            System.out.println("Decrypted data from database: ");
        }
        else {
            int i = 0;
            for (int row = 0; documents[i] != null; ++row) {
                int iname;
                for (iname = 0; !documents[i].startsWith("id"); documents[i] = documents[i].substring(1)) {
                }
                while (documents[i].length() > 0) {
                    String temp;
                    for (temp = ""; !documents[i].startsWith("="); documents[i] = documents[i].substring(1)) {
                        temp = temp + documents[i].charAt(0);
                    }
                    while (name[iname] != null && !name[iname].equals(temp)) {
                        ++iname;
                    }
                    name[iname] = temp;
                    temp = "";
                    for (documents[i] = documents[i].substring(1); !documents[i].startsWith(",") && !documents[i].startsWith("}"); documents[i] = documents[i].substring(1)) {
                        temp = temp + documents[i].charAt(0);
                    }
                    if (decrypt && name[iname].equals("email") && name[iname].equals("phone")) {
                        temp = aes.decrypt(temp + "==");
                    }
                    data[row][iname] = temp;
                    documents[i] = documents[i].substring(2);
                }
                ++i;
            }
            System.out.println("Data from database: ");
        }
        print_data();
    }

    public static void print_data() {
        int i;
        for(i = 0; name[i] != null; ++i) {
            System.out.print(name[i] + " | ");
        }

        System.out.println();

        for(i = 0; i < data.length && data[i] != null; ++i) {
            for(int j = 0; data[i][j] != null; ++j) {
                System.out.print(data[i][j] + " | ");
            }

            System.out.println();
        }

    }
}