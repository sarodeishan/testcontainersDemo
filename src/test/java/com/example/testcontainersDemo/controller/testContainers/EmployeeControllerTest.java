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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class EmployeeControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    private static int localServerPort = 3306;

    // if testcontainer is not static then a new docker container will be started for every test, otherwise only one docker instance will be shared for all testcases.
    // Since spring boot application only establishes DB connection at the app initialisation, and not for every test case in the class. We will be sharing the single docker container.
    //    docker run --name=mysql1 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mypassword -e MYSQL_DATABASE=mysql1 -d mysql
    @Container
    public static GenericContainer mysql = new GenericContainer(DockerImageName.parse("mysql:latest"))
            .withEnv("MYSQL_ROOT_PASSWORD", "mypassword")
            .withEnv("MYSQL_DATABASE", "mysql1")
            .withExposedPorts(localServerPort);

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", () -> "jdbc:mysql://" + mysql.getHost() + ":" + mysql.getFirstMappedPort() + "/mysql1");
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