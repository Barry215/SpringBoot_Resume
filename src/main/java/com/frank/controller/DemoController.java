package com.frank.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by frank on 17/3/9.
 */
@RestController
public class DemoController {

    @RequestMapping("/hello")
    public String index() {
        return "Hello World";
    }
}
