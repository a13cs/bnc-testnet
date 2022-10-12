package com.example.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApi {

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String accInfo() {
        return "ok";
    }

}
