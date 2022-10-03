package com.example.testcontainersDemo.service;

import com.example.testcontainersDemo.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeDao {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Employee> getEmployees() {
        return jdbcTemplate.query("SELECT employeeId, name, department, age FROM employee",
                (rs, rowNum) -> new Employee(rs.getInt("employeeId"), rs.getString("name"), rs.getString("department"), rs.getInt("age"))
        );
    }

    public Employee getEmployee(int id) {
        return jdbcTemplate.queryForObject("SELECT employeeId, name, department, age FROM employee WHERE employeeId=" + id,
                (rs, rowNum) -> new Employee(rs.getInt("employeeId"), rs.getString("name"), rs.getString("department"), rs.getInt("age"))
        );
    }

    public int deleteEmployee(int id) {
        return jdbcTemplate.update("DELETE FROM employee e WHERE e.employeeId=" + id);
    }


}
