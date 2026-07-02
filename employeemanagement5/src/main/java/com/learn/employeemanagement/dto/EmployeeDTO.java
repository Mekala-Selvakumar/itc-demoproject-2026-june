package com.learn.employeemanagement.dto;

 
public class EmployeeDTO {

    private String employeeName;

    private String email;

    private String department;

    private Double salary;

    public EmployeeDTO() {
    }

    public EmployeeDTO(String employeeName,
                       String email,
                       String department,
                       Double salary) {
        this.employeeName = employeeName;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
}