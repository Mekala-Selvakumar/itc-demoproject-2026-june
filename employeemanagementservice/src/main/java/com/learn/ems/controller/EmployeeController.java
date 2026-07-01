package com.learn.ems.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.learn.ems.entity.Employee;
import com.learn.ems.service.EmployeeService;

@RestController
public class EmployeeController {

    @Autowired
    EmployeeService service;

    @PostMapping("/employees")
    public Employee save(@RequestBody Employee employee){

        return service.save(employee);

    }

}