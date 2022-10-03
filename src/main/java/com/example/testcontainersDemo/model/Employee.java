package com.example.testcontainersDemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private int employeeId;

    private String name;

    private String department;

    private long age;

}
