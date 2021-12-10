import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Console;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Main {
    public static Properties configProperties = new Properties();

    public static void main(String[] args) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        Logger logger = Logger.getLogger("org.mongodb.driver");
        logger.setLevel(Level.SEVERE);

        Scanner sc = new Scanner(System.in);


        FileInputStream fileInputStream = new FileInputStream("C:/Users/Malii Antonela/Desktop/CS_LAB8-Main/src/main/java/config.properties");
        configProperties.load(fileInputStream);

        MongoClientURI uri = new MongoClientURI("mongodb+srv://antonelaw:" + configProperties.getProperty("dbpassword") + "@cluster0.wjrcv.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("myFirstDatabase");

        MongoCollection<Document> collection = database.getCollection("testcollection1");

        boolean flag = true;
        while(flag) {
            System.out.println("1 - register , 2 - authenticate, 3 - exit program");
            String option = sc.nextLine();
            switch (option) {
                case "1":
                    registerNewUser(collection);
                    break;
                case "2":
                    authenticate(collection);
                    break;
                case "3":
                    flag = false;
                    break;
            }
        }

    }


    public static void registerNewUser(MongoCollection<Document> collection) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Scanner sc = new Scanner(System.in);
        Scanner scanner = null;
        Console console = System.console();
        String email;
        String password;
        while (true) {

            if(console == null){
                scanner = new Scanner(System.in);
                System.out.println("Enter email: ");
                email = scanner.next();
                System.out.println("Enter your password");
                password = scanner.next();
                password = Hash.generateHash(password);
                break;
            }
            else{
                System.out.println("Enter email: ");
                email = console.readLine();
                System.out.println("Enter your password");
                password = console.readLine();
                password = Hash.generateHash(password);
                break;
            }

        }

        String checkString = generateString(30);
        sendEmail(email, checkString);

        System.out.println("Please insert received code");
        while (true) {
            String code = sc.nextLine();
            if(code.equals(checkString)) {
                System.out.println("Registration complete, welcome!!!");
                Document document = new Document("email", email).append("password", password);
                collection.insertOne(document);
                break;
            }
            else {
                System.out.println("Verification code incorrect, please try again");
                continue;
            }
        }
    }

    public static void authenticate(MongoCollection<Document> collection) throws InvalidKeySpecException, NoSuchAlgorithmException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter your email");
        String email = sc.nextLine();
        System.out.println("Enter your password");
        char[] password = System.console().readPassword();

        long count = collection.countDocuments(new Document("email", email));
        if(count != 1) {
            System.out.println("Invalid email/password");
            return ;
        }
        else {
            BasicDBObject query = new BasicDBObject();
            query.put("email", email);
            FindIterable<Document> iterDoc =  collection.find(query);
            int i = 1;
            Iterator it = iterDoc.iterator();
            while(it.hasNext()) {
                Document document = (Document) it.next();
                if(Hash.validatePassword(String.valueOf(password), document.getString("password")))
                    System.out.println("Successfully authenticated, welcome");
                else
                    System.out.println("Invalid email/password");
            }
        }
    }

    public static void sendEmail(String to, String checkString) {
        String from = configProperties.getProperty("confirmation_email");
        String host = "smtp.gmail.com";

        final Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(configProperties.getProperty("confirmation_email"), configProperties.getProperty("confirmation_email_password"));

            }

        });

        session.setDebug(false);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("App confirmation code");
            message.setText(checkString);
            Transport.send(message);
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
    public static String generateString(int n) {

        byte[] arr = new byte[256];
        new Random().nextBytes(arr);

        String randString = new String(arr, Charset.forName("UTF-8"));
        StringBuffer stringBuffer = new StringBuffer();

        for(int k = 0; k < randString.length(); k++) {
            char ch = randString.charAt(k);
            if(((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) && (n > 0)) {
                stringBuffer.append(ch);
                n--;
            }
        }

        return stringBuffer.toString();
    }
    public static boolean isValid(String email) {

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }
}
