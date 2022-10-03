package com.example.testcontainersDemo.controller.mockmvc;

import com.example.testcontainersDemo.model.Employee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

//as jdbctemplate is mocked spring app doesnt try to establish a db connection
@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class EmployeeControllerMockTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JdbcTemplate jdbcTemplate;

    @Test
    void mockitoTest() throws Exception {
        Mockito.when(jdbcTemplate.query(Mockito.anyString(), Mockito.any(RowMapper.class))).thenReturn(Arrays.asList(new Employee(1, "mock", "test", 25)));
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.get("/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andReturn();
        List<Employee> employees = this.objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<Employee>>() {
        });
        Mockito.verify(jdbcTemplate, Mockito.times(1)).query(Mockito.anyString(), Mockito.any(RowMapper.class));
        System.out.println("Recieved response:" + employees);
        Assertions.assertThat(employees).hasSize(1);
    }

}