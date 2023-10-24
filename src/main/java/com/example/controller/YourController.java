package com.example.controller;

import com.example.services.ElasticsearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@ComponentScan(basePackages = "com.example.services")
public class YourController {

    static List<String> indexNamesList = new ArrayList<>(Arrays.asList("Laptops", "Phones"));

    private final ElasticsearchService elasticsearchService;

    public YourController(ElasticsearchService elasticsearchService) {
        this.elasticsearchService = elasticsearchService;
    }

    static HttpHeaders responseHeaders = new HttpHeaders();

    static {
        responseHeaders.set("Access-Control-Allow-Credentials", "true");
        responseHeaders.set("Access-Control-Allow-Headers", "x-total-count, Content-Type, Content-Length, Date");
        responseHeaders.set("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH");
        responseHeaders.set("Access-Control-Expose-Headers", "x-total-count, Content-Type, Content-Length, Date");
        responseHeaders.set("Content-Type", "application/json;charset=UTF-8");
        responseHeaders.set("Content-Type", "application/json; charset=utf-8");
        responseHeaders.set("X-Content-Type-Options", "nosniff");
        responseHeaders.set("Content-Type", "application/json;charset=UTF-8");
    }

    @GetMapping("/search-all")
    public SearchResponse searchAll() {
        try {
            return elasticsearchService.searchAllDocuments("laptops");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/products")
    public ResponseEntity<String> searchProductsByTitle(@RequestParam(value = "id", required = false) Integer id) {
        return ResponseEntity.ok(String.valueOf(elasticsearchService.searchProductsById(id, indexNamesList)));
    }

    @GetMapping("/products/getForSearch")
    public ResponseEntity<String> sendProductsForSearch() throws JSONException, IOException {
        return new ResponseEntity<>(String.valueOf(elasticsearchService.getProductsForSearch(indexNamesList)), responseHeaders, HttpStatus.OK);
    }

    @PostMapping("/favourites/get")
    public ResponseEntity<String> sendFavourites(@RequestBody String ids) {
        JSONArray transformedResult = new JSONArray();

        for (String id : new JSONObject(ids).getJSONArray("favourites").toList().stream().map(Object::toString).toList()) {
            try {
                transformedResult.put(elasticsearchService.searchProductsById(Integer.parseInt(id), indexNamesList));
            } catch (Exception ignored) {
            }
        }

        return new ResponseEntity<>(String.valueOf(transformedResult), responseHeaders, HttpStatus.OK);
    }

    @GetMapping("/products/ForComparison/{type}/{property}")
    public ResponseEntity<Resource> getForComparison(@PathVariable String property, @PathVariable String type) {
        return ResponseEntity.ok().headers(responseHeaders).body(new ClassPathResource(type + "/" + property + ".json"));
    }

    @GetMapping("/products/tops")
    public ResponseEntity<String> getTop() {
        JSONArray req = new JSONArray();


        List<String> typeTopList = new ArrayList<>(List.of("Топ телефонов до 20 000", "Топ телефонов-флагманов", "Топ ноутбуков до 50 000"));

        for (String title : typeTopList) {
            List<Integer> idList = new ArrayList<>();
            switch (title) {
                case "Топ телефонов до 20 000" -> idList = new ArrayList<>(Arrays.asList(133, 70, 150, 262, 108, 244, 242, 24, 166));
                case "Топ телефонов-флагманов" -> idList = new ArrayList<>(Arrays.asList(4, 143, 9, 43, 170, 74, 265, 150));
                case "Топ ноутбуков до 50 000" -> idList = new ArrayList<>(Arrays.asList(10003, 10007, 10008, 10016, 10017, 10020));
            }
            JSONObject oneObject = new JSONObject();
            oneObject.put("title", title);
            for (Integer id : idList) {
                try {
                    oneObject.append("products", elasticsearchService.searchProductsById(id, indexNamesList));
                } catch (Exception ignored) {
                }
            }
            req.put(oneObject);
        }
        return new ResponseEntity<>(String.valueOf(req), responseHeaders, HttpStatus.OK);
    }

    @PostMapping("products/search/withFilter")
    public ResponseEntity<String> sendSearchWithFilter2(@RequestBody String stringRequest) throws IOException {
        JSONObject js = elasticsearchService.getSearchObjectsWithFilter(stringRequest);
        return new ResponseEntity<>(String.valueOf(js), responseHeaders, HttpStatus.OK);
    }

}

