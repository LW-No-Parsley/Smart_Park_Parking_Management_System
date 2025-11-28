package com.smart_park_parking_management_system.controller;

import com.smart_park_parking_management_system.common.R;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/test1")
    public R<List<Student>> getStudent() {
        ArrayList<Student> list = new ArrayList<>();
        Student student1 = new Student();
        student1.setId(1);
        student1.setName("name1");
        Student student2 = new Student();
        student2.setId(2);
        student2.setName("name2");
        list.add(student1);
        list.add(student2);
        return R.success(list);
    }
    @PostMapping("/test2")
    public void test() {
        throw new RuntimeException("发生异常");
    }
}

@Data
class Student {
    private Integer id;
    private String name;
}

