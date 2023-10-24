package com.example.controller;

import com.example.DAO.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("")
@CrossOrigin
public class MainController {
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
/*

    @GetMapping("/products/getForSearch")
    public ResponseEntity<String> sendProductsForSearch() throws JSONException {
        return new ResponseEntity<>(String.valueOf(ProductsDAO.getProductsForSearch()), responseHeaders, HttpStatus.OK);
    }

    @GetMapping("/products")
    public ResponseEntity<String> sendProducts(@RequestParam(value = "id", required = false) Integer id, @RequestParam(value = "limit", required = false) Integer limit, @RequestParam(value = "page", required = false) Integer page) throws JSONException {
        responseHeaders.set("x-total-count", String.valueOf(ProductsDAO.NumberOfProducts()));

        return id != null ? new ResponseEntity<>(String.valueOf(ProductsDAO.getById(id)), responseHeaders, HttpStatus.OK) : new ResponseEntity<>(String.valueOf(ProductsDAO.getAllObjects(limit / 2, page)), responseHeaders, HttpStatus.OK);
    }

    @PostMapping("/favourites/get")
    public ResponseEntity<String> sendFavourites(@RequestBody String ids) {
        return new ResponseEntity<>(String.valueOf(ProductsDAO.getFavourites(new JSONObject(ids).getJSONArray("favourites").toList())), responseHeaders, HttpStatus.OK);
    }

    @PostMapping("/products/search/withFilter")
    public ResponseEntity<String> sendSearchWithFilter(@RequestBody String stringRequest) {
        return new ResponseEntity<>(String.valueOf(ProductsDAO.getSearchObjectsWithFilter(stringRequest)), responseHeaders, HttpStatus.OK);
    }

    @GetMapping("/products/ForComparison/{type}/{property}")
    public ResponseEntity<Resource> getForComparison(@PathVariable String property, @PathVariable String type) {
        return ResponseEntity.ok().headers(responseHeaders).body(new ClassPathResource(type + "/" + property + ".json"));
    }

    @GetMapping("/products/top/{type}/{property}")
    public ResponseEntity<String> getTop(@PathVariable String type, @PathVariable String property) {

        List<Object> idList = new ArrayList<>();

        if (type.equals("Phones")) {
            switch (property) {
                case ("flagship") -> idList = new ArrayList<>(Arrays.asList(4, 414, 143, 9, 416, 43, 170, 429, 74, 265, 333, 402, 150));
                case ("20k") -> idList = new ArrayList<>(Arrays.asList(133, 70, 150, 333, 262, 463, 434, 432, 401, 108, 450, 244, 242, 24, 166, 375));
            }

        } else if (type.equals("Laptops") && property.equals("50k")) {
            idList = new ArrayList<>(Arrays.asList(10446, 10853, 10035, 10492, 10842, 10520, 10410, 10420));
        }
        return new ResponseEntity<>(String.valueOf(ProductsDAO.getFavourites(idList)), responseHeaders, HttpStatus.OK);
    }
*/

}
