package com.learn.ems.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.learn.ems.entity.Employee;
import com.learn.ems.repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    EmployeeRepository repository;

    public Employee save(Employee employee){

        if(employee!=null){

            if(employee.getSalary()>0){

                if(employee.getDepartment()!=null){

                    repository.save(employee);

                }

            }

        }

        return employee;

    }

}