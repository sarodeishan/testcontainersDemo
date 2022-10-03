package com.example.testcontainersDemo.controller.testContainers;

import com.example.testcontainersDemo.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class EmployeeControllerMySQLTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    //docker run --name=mysql1 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mypassword -e MYSQL_DATABASE=mysql1 -d mysql
    @Container
    public static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:latest"));

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", mysql::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", mysql::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void getAllEmp() throws Exception {
//        Thread.sleep(20000);
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.get("/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        List<Employee> employees = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Employee>>() {
        });
        Assertions.assertThat(employees).hasSize(4);
    }

}