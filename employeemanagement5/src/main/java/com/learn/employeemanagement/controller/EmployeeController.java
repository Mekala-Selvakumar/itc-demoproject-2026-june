package com.learn.employeemanagement.controller;

 
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.learn.employeemanagement.dto.EmployeeDTO;
import com.learn.employeemanagement.entity.Employee;
import com.learn.employeemanagement.service.EmployeeService;

 
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    @PostMapping
    public Employee saveEmployee(@RequestBody EmployeeDTO dto) {

        return service.saveEmployee(dto);

    }

    @GetMapping
    public List<Employee> getAllEmployees() {

        return service.getAllEmployees();

    }

    @GetMapping("/{id}")
    public Employee getEmployee(@PathVariable Long id) {

        return service.getEmployee(id);

    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id,
                                   @RequestBody EmployeeDTO dto) {

        return service.updateEmployee(id, dto);

    }

    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id) {

        service.deleteEmployee(id);

        return "Employee deleted successfully";

    }

}