package com.learn.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.ems.entity.Employee;

public interface  EmployeeRepository  extends JpaRepository<Employee, Long> {

}
