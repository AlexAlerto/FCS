package com.example.DAO;

import com.mongodb.client.*;
import io.joshworks.restclient.http.HttpResponse;
import io.joshworks.restclient.http.Unirest;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class ProductsDAO {
    private static final String DATABASE_NAME = "products";

    private static final List<String> COLLECTION_NAMES = new ArrayList<>(Arrays.asList("Phones", "Laptops"));


    public static long NumberOfProducts() {
        int count = 0;
        for (int i = 0; i < COLLECTION_NAMES.size(); i++) {
            count += getMongoCursors(i).countDocuments();
        }
        return count;
    }

    public static LinkedList<String> getProductsForSearch() throws JSONException {

        LinkedList<String> titles = new LinkedList<>();
        for (int i = 0; i < COLLECTION_NAMES.size(); i++) {
            for (Document document : getMongoCursors(i).find().projection(new Document("title", 1))) {
                String title = document.getString("title");
                titles.add(title);
            }
        }

        return titles;
    }

    public static JSONObject getById(int id) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < COLLECTION_NAMES.size(); i++) {
            for (Document document : getMongoCursors(i).find(new Document("_id", Integer.toString(id)))) {
                jsonObject = new JSONObject(document.toJson());
                jsonObject.put("type", COLLECTION_NAMES.get(i));
            }
        }
        return jsonObject;

    }

    public static JSONArray getFavourites(List<Object> idList) throws JSONException {
        return getJSONArrayFromQuery(new Document("_id", new Document("$in", idList.stream().map(Object::toString).collect(Collectors.toList()))));
    }

    public static JSONArray getAllObjects(int limit, int page) throws JSONException {


        List<Document> list1 = new ArrayList<>();
        for (int i = 0; i < COLLECTION_NAMES.size(); i++) {
            for (Document document : getMongoCursors(i).find().skip(limit * (page - 1)).limit(limit)) {
                document.append("type", COLLECTION_NAMES.get(i));
                list1.add(document);
            }
        }

        Collections.shuffle(list1);

        JSONArray jsonArray1 = new JSONArray();
        for (Document document : list1) {
            JSONObject jsonObject = new JSONObject(document.toJson());
            jsonArray1.put(jsonObject);
        }
        return jsonArray1;
    }

    public static JSONArray getSearchObjects(String title) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        String[] keywords = title.split("\\s+");

        for (int i = keywords.length; i > 0; i--) {
            String keyword = String.join("\\s+", Arrays.copyOfRange(keywords, 0, i));

            mergeJSONArrayWithoutDuplicates(jsonArray, getJsonArray(getMongoCursors(0)
                    .find(new Document("description", new Document("$regex", keyword + "\\b").append("$options", "i")))
                    .iterator()), "Phones");

            mergeJSONArrayWithoutDuplicates(jsonArray, getJsonArray(getMongoCursors(1)
                    .find(new Document("title", new Document("$regex", keyword + "\\b").append("$options", "i")))
                    .iterator()), "Laptops");
        }
        return jsonArray;
    }

    public static JSONObject getSearchObjectsWithFilter(String stringRequest) throws JSONException {
        JSONObject jsonRequest = new JSONObject(stringRequest);

        String title = jsonRequest.getString("search").replaceAll(" ", "+");

        HttpResponse<String> response;

        try {
            response = Unirest.get("https://localhost:8443/API/getForSearch?title=" + title)
                    .asString();

        } catch (Exception e) {
            response = Unirest.get(title).asString();
        }

        JSONArray filteredArray = new JSONArray();
        JSONObject filteredObj = new JSONObject();

        ProductsDAO.getSearchObjects(response.body()).forEach(type -> {
            if (type instanceof JSONObject typeObject) {
                if (jsonRequest.getString("type").equals(typeObject.getString("type")) || jsonRequest.getString("type").equals("none")) {
                    if (matchesFilters(typeObject.getJSONObject("property"), jsonRequest.getJSONArray("filters"))) {
                        filteredArray.put(typeObject);
                    }
                }
            }
        });

        JSONObject js = new JSONObject();

        Map<String, Map<String, Integer>> filterCounts = new HashMap<>();

        filteredArray.forEach(property -> {
            if (property instanceof JSONObject propertyObject) {
                JSONObject propertyDetails = propertyObject.getJSONObject("property");
                try {
                    String[] keys = JSONObject.getNames(propertyDetails);
                    if (keys != null) {
                        for (String key : keys) {
                            js.append(key, propertyDetails.getString(key));

                            String value = propertyDetails.getString(key);

                            filterCounts.computeIfAbsent(key, k -> new HashMap<>())
                                    .compute(value, (k, v) -> v == null ? 1 : v + 1);
                        }
                    }

                } catch (JSONException ignored) {
                }
            }
        });

        JSONObject finalFilters = new JSONObject();
        for (Map.Entry<String, Map<String, Integer>> entry : filterCounts.entrySet()) {
            JSONObject valueObject = new JSONObject();
            for (Map.Entry<String, Integer> valueEntry : entry.getValue().entrySet()) {
                valueObject.put(valueEntry.getKey(), valueEntry.getValue());
            }
            finalFilters.put(entry.getKey(), valueObject);
        }

        filteredObj.put("filters", finalFilters);
        filteredObj.put("products", filteredArray);
        return filteredObj;
    }



    private static boolean matchesFilters(JSONObject properties, JSONArray filters) {
        for (int i = 0; i < filters.length(); i++) {
            JSONArray filter = filters.getJSONArray(i);
            String propertyName = filter.getString(0);
            String propertyValue = filter.getString(1);

            if (!properties.has(propertyName) || !properties.getString(propertyName).contains(propertyValue)) {
                return false;
            }
        }
        return true;
    }

    private static void mergeJSONArrayWithoutDuplicates(JSONArray jsonArray, JSONArray newArray, String name) throws JSONException {
        for (int i = 0; i < newArray.length(); i++) {
            JSONObject newObj = newArray.getJSONObject(i);

            if (!containsObject(jsonArray, newObj)) {
                jsonArray.put(newObj.put("type", name));
            }
        }
    }

    private static boolean containsObject(JSONArray jsonArray, JSONObject obj) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject existingObj = jsonArray.getJSONObject(i);

            if (existingObj.get("_id").equals(obj.get("_id"))) {
                return true;
            }
        }
        return false;
    }

    private static MongoCollection<Document> getMongoCursors(int i) throws JSONException{
        return MongoClients.create("mongodb://localhost:27017").getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAMES.get(i));
    }

    private static JSONArray getJSONArrayFromQuery(Document query) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < COLLECTION_NAMES.size(); i++) {
            mergeJSONArray(jsonArray, getJsonArray(getMongoCursors(i).find(query).iterator()), COLLECTION_NAMES.get(i));
        }
        return jsonArray;
    }

    private static JSONArray getJsonArray(MongoCursor<Document> cursor) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        while (cursor.hasNext()) {
            Document document = cursor.next();
            JSONObject jsonObject = new JSONObject(document.toJson());
            jsonArray.put(jsonObject);
        }

        cursor.close();
        return jsonArray;
    }

    private static void mergeJSONArray(JSONArray jsonArray1, JSONArray jsonArray2, String name) throws JSONException {
        for (int i = 0; i < jsonArray2.length(); i++) {
            jsonArray1.put(jsonArray2.getJSONObject(i).put("type", name));
        }
    }
}
