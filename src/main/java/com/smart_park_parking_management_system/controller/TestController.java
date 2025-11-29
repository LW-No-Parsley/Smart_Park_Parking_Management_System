package com.smart_park_parking_management_system.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TestController {
    @PostMapping("/test1")
    public void test(@RequestParam Integer id, @RequestParam String name) {
        System.out.println(id + name);
        boolean equals = name.equals("11");
    }
}

