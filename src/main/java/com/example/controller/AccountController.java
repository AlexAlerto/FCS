package com.example.controller;


import com.example.DAO.AccountsDAO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("")
public class AccountController {

    static HttpHeaders responseHeaders = new HttpHeaders();

    static {
        responseHeaders.set("Access-Control-Allow-Credentials", "true");
        responseHeaders.set("Access-Control-Allow-Headers", "x-total-count, Content-Type, Content-Length, Date, Set-Cookie");
        responseHeaders.set("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH");
        responseHeaders.set("Access-Control-Expose-Headers", "x-total-count, Content-Type, Content-Length, Date, Set-Cookie");
        responseHeaders.set("Content-Type", "application/json;charset=UTF-8");
        responseHeaders.set("Content-Type", "application/json; charset=utf-8");
        responseHeaders.set("X-Content-Type-Options", "nosniff");
        responseHeaders.set("Content-Type", "application/json;charset=UTF-8");
    }

    @GetMapping("/deleteCookie")
    public ResponseEntity<String> deleteCookie() {
        responseHeaders.remove("Set-Cookie");
        return ResponseEntity.ok()
                .header("Set-Cookie", AccountsDAO.expireCookie().toString())
                .headers(responseHeaders)
                .body("Cookie is deleted");
    }

    private String handleResponse(String user, ResponseCookie cookie) throws JSONException {
        JSONObject responseJson = new JSONObject().put("message", new JSONObject(user).getString("login")).put("userData", new JSONObject().put("login", new JSONObject(user).getString("login")).put("favourites", new JSONArray(AccountsDAO.lists(cookie.getValue(), null, "favorites"))));
        responseHeaders.set(HttpHeaders.SET_COOKIE, cookie.toString());
        return responseJson.toString();
    }

    @PostMapping("/reg")
    public ResponseEntity<String> registration(@RequestBody String user) throws JSONException {
        ResponseCookie cookie = AccountsDAO.returnCookie();
        if (AccountsDAO.registration(new JSONObject(user).put("cookie", cookie.getValue()))) {
            return new ResponseEntity<>(handleResponse(user, cookie), responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Пользователь с таким логином уже существует", responseHeaders, HttpStatus.OK);
        }
    }

    @PostMapping("/log")
    public ResponseEntity<String> login(@RequestBody String user) throws JSONException {
        ResponseCookie cookie = AccountsDAO.returnCookie();
        if (AccountsDAO.login(new JSONObject(user).put("cookie", cookie.getValue()))) {
            return new ResponseEntity<>(handleResponse(user, cookie), responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Неправильный логин или пароль", responseHeaders, HttpStatus.OK);
        }
    }

    @PostMapping("/rec")
    public ResponseEntity<String> passwordRecovery(@RequestBody String user) throws JSONException {
        ResponseCookie cookie = AccountsDAO.returnCookie();
        if (AccountsDAO.recovery(new JSONObject(user).put("cookie", cookie.getValue()))) {
            return new ResponseEntity<>(handleResponse(user, cookie), responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Неправильный логин или пароль", responseHeaders, HttpStatus.OK);
        }
    }


    @GetMapping(path = "/editFavourite")
    public ResponseEntity<String> editFavourites(@CookieValue(value = "JSESSIONID", required = false) String cookieValue, @RequestParam(value = "id", required = false) Integer id) {
        if (cookieValue == null) {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body("local");
        } else {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(String.valueOf(AccountsDAO.lists(cookieValue, id, "favorites")));
        }

    }


    @GetMapping(path = "/editComparison")
    public ResponseEntity<String> editComparisons(@CookieValue(value = "JSESSIONID", required = false) String cookieValue, @RequestParam(value = "id", required = false) Integer id) {
        if (cookieValue == null) {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body("local");
        } else {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(String.valueOf(AccountsDAO.lists(cookieValue, id, "comparisons")));
        }

    }

    @GetMapping("/checkCookie")
    public ResponseEntity<String> checkCookie(@CookieValue(value = "JSESSIONID", required = false) String cookieValue) throws JSONException {
        responseHeaders.remove("Set-Cookie");
        if (!Objects.equals(cookieValue, "")) {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(Objects.requireNonNull(AccountsDAO.checkCookie(cookieValue)).toString());
        } else {
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body("local");
        }
    }
}
