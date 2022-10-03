package com.example.testcontainersDemo.controller.testContainers;

import com.example.testcontainersDemo.model.Employee;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

@Testcontainers
public class EmployeeControllerNetworkTest {

    static Network network = Network.newNetwork();

    //docker run --name=mysql1 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mypassword -e MYSQL_DATABASE=mysql1 -d mysql
    @Container
    public static MySQLContainer mysql = (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:latest"))
            .withDatabaseName("mysql1")
            .withPassword("mypassword")
            .withNetwork(network)
            .withNetworkAliases("mysql", "mysqlserver");

    @Container
    public static GenericContainer app = new GenericContainer(new ImageFromDockerfile()
            .withFileFromFile("Dockerfile", new File("./Dockerfile"))
            .withFileFromFile("target", new File("./target")))
            .dependsOn(mysql)
            .withExposedPorts(8080)
            .withNetwork(network)
            .withNetworkAliases("App")
            .withEnv("SPRING_PROFILES_ACTIVE", "network")
            .waitingFor(Wait.forHttp("/employees").forStatusCode(200));

    private static int port;
    private static String host;

    @BeforeAll
    static void init() {
        host = app.getHost();
        port = app.getFirstMappedPort();
    }

    @Test
    void getAllEmp() throws InterruptedException {
        Thread.sleep(20000);
        System.out.println("Exposed port:" + port);
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://" + host + ":" + port + "/employees";

        ResponseEntity<List<Employee>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Employee>>() {
        });
        System.out.println(response);
        Assertions.assertThat(response.getBody()).hasSize(4);
    }

}