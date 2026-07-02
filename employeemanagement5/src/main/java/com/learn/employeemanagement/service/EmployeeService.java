package com.learn.employeemanagement.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.learn.employeemanagement.dto.EmployeeDTO;
import com.learn.employeemanagement.entity.Employee;
import com.learn.employeemanagement.repository.EmployeeRepository;
 
@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository repository;

    public Employee saveEmployee(EmployeeDTO dto) {

        Employee employee = new Employee();

        employee.setEmployeeName(dto.getEmployeeName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setSalary(dto.getSalary());

        return repository.save(employee);
    }

    public List<Employee> getAllEmployees() {

        return repository.findAll();
    }

    public Employee getEmployee(Long id) {

        return repository.findById(id).orElse(null);
    }

    public Employee updateEmployee(Long id, EmployeeDTO dto) {

        Employee employee = repository.findById(id).orElse(null);

        if (employee != null) {

            employee.setEmployeeName(dto.getEmployeeName());
            employee.setEmail(dto.getEmail());
            employee.setDepartment(dto.getDepartment());
            employee.setSalary(dto.getSalary());

            repository.save(employee);
        }

        return employee;
    }

    public void deleteEmployee(Long id) {

        repository.deleteById(id);
    }

}
