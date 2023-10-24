package com.example.DAO;


import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseCookie;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;

public class AccountsDAO {
    private static final String DATABASE_NAME = "products";
    static MessageDigest sha1;

    static {
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    public static boolean registration(JSONObject user) {
        MongoCollection<Document> collection = getMongoCollections();
        MongoCursor<Document> cursor = collection.find(new Document("login", user.getString("login"))).iterator();

        if (cursor.hasNext()) {
            return false;
        } else {
            Document newDocument = new Document("login", user.getString("login"))
                    .append("pass", String.format("%032x", new java.math.BigInteger(1, sha1.digest(user.getString("pass").getBytes()))))
                    .append("cookie", user.getString("cookie"))
                    .append("favorites", new ArrayList<>())
                    .append("comparisons", new ArrayList<>());
            collection.insertOne(newDocument);
            return true;
        }
    }

    public static boolean login(JSONObject user) {
        MongoCollection<Document> collection = getMongoCollections();
        MongoCursor<Document> cursor = collection.find(new Document("login", user.getString("login"))).iterator();
        Document existingDocument = collection.find(new Document("login", user.getString("login"))).first();

        if (cursor.hasNext() && existingDocument != null) {
            if (cursor.next().getString("pass").equals(String.format("%032x", new java.math.BigInteger(1, sha1.digest(user.getString("pass").getBytes()))))) {
                existingDocument.put("cookie", user.getString("cookie"));
                collection.replaceOne(new Document("login", user.getString("login")), existingDocument);
                return true;
            }
        }
        return false;
    }

    public static boolean recovery(JSONObject user) {
        MongoCollection<Document> collection = getMongoCollections();
        Document existingDocument = collection.find(new Document("login", user.getString("login"))).first();

        if (existingDocument != null) {
            if (existingDocument.getString("pass").equals(String.format("%032x", new java.math.BigInteger(1, sha1.digest(user.getString("oldPass").getBytes()))))) {
                existingDocument.put("pass", String.format("%032x", new java.math.BigInteger(1, sha1.digest(user.getString("pass").getBytes()))));
                existingDocument.put("cookie", user.getString("cookie"));
                collection.replaceOne(new Document("login", user.getString("login")), existingDocument);
                return true;
            }
        }
        return false;
    }

    public static ResponseCookie returnCookie() {
        return ResponseCookie.from("JSESSIONID", UUID.randomUUID().toString())
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(1))
                .sameSite("None")
                .build();
    }

    public static ResponseCookie expireCookie() {
        return ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(true)
                .maxAge(-1)
                .sameSite("None")
                .build();
    }

    public static JSONObject checkCookie(String cookie) {
        MongoCollection<Document> collection = getMongoCollections();
        MongoCursor<Document> cursor = collection.find(new Document("cookie", cookie)).iterator();
        JSONObject jsonObject = new JSONObject();

        if (cursor.hasNext()) {
            JSONObject jsonObject2 = new JSONObject(cursor.next());
            if (cookie.equals(jsonObject2.getString("cookie"))) {
                jsonObject.put("message", "asd").put("userData", new JSONObject().put("favourites", new JSONArray(lists(cookie, null, "favorites"))).put("login", jsonObject2.getString("login")));
            }
        } else {
            jsonObject.put("message", "вашего кукки нет");
        }
        return jsonObject;
    }

    public static String lists(String cookie, Integer id, String list) {
        MongoCollection<Document> collection = getMongoCollections();
        MongoCursor<Document> cursor = collection.find(new Document("cookie", cookie)).iterator();
        Document existingDocument = collection.find(new Document("cookie", cookie)).first();

        if (cursor.hasNext() && existingDocument != null) {
            Set<Integer> integerList = new HashSet<>(cursor.next().getList(list, Integer.class));

            if (id == null) {
                return integerList.toString();
            } else if (!integerList.contains(id)) {
                integerList.add(id);
            } else {
                integerList.remove(id);
            }
            existingDocument.put(list, integerList);
            collection.replaceOne(new Document(new Document("cookie", cookie)), existingDocument);
            return integerList.toString();
        }
        return "Сессия устарела";
    }


    private static MongoCollection<Document> getMongoCollections() throws JSONException {
        return MongoClients.create("mongodb://localhost:27017").getDatabase(DATABASE_NAME).getCollection("Users");
    }
}
