package com.example.testcontainersDemo.controller;

import com.example.testcontainersDemo.model.Employee;
import com.example.testcontainersDemo.service.EmployeeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class EmployeeController {

    private EmployeeDao employeeDao;

    @Autowired
    public EmployeeController(EmployeeDao employeeDao) {
        this.employeeDao = employeeDao;
    }

    @GetMapping(path = "/employees", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok().body(employeeDao.getEmployees());
    }

    @GetMapping(path = "/employee/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Employee> getEmployee(@PathVariable("id") int id) {
        return ResponseEntity.ok().body(employeeDao.getEmployee(id));
    }

    @DeleteMapping(path = "/employee/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") int id) {
        employeeDao.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

}