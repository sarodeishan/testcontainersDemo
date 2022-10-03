# testcontainersDemo

Demo repository showing how the test containers can be useful for testing

### About the Project

- **Java 8 runtime**
- **Docker**
- **Spring Boot**
- **Testcontainers**

The project is generated through spring initializer.
The project is managed through maven and is a simple CRUD application for employee database.
The project exposes itself to the world through a simple REST API layer with below endpoints:

- GET /employees
- GET /employee/{id}
- DELETE /employee/{id}

The project depends upon a mysql database store all employee data and makes a mysql db connection during startup.

```
#db
spring.datasource.url=jdbc:mysql://localhost:3306/mysql1
spring.datasource.username=root
spring.datasource.password=mypassword
spring.sql.init.mode=always
```

This external dependency of a database for a project causes challenges when developing on local machine and/or
unit/integration testing of the project.
While UT tests a particular method in a class, an integration test is supposed to be testing the whole application as a
one.
The project is more focussed toward implementing the standalone integration test but the same methodology can be easily
implemented for Unit tests.

As the project needs one external dependency, we can do either of the 3 things to run the application:

1. Mock the database entirely on application layer.(its the easiest to do but doesn't really tests e2e)
2. Connect with an actual MySql Database on the network(most often it's not possible and non-advisable for standalone
   tests)
3. Create a MySql database of our own on local machine(this is difficult to create and manage)

The project will guide through on how different testing utilities and see how testcontainers framework can tackle this
problem in step by step manner and make the dev's life easier and more confident.

### Testing approaches and frameworks

1. Mocking the Mysql DB with Mockito framework

    * **Class Name: mockmvc\EmployeeControllerMockTest**
    * As the project is built on Spring Boot, we have a great testing support where the whole application can be tested
      directly using Spring Boot Testing framework.
    * The annotation @SpringBootTest on a test class will initialise the whole spring context, load all the libraries
      needed and deploy the Spring Boot Application temporarily on local machine before running any test.
    * So technically, the whole application is deployed on our local machine inside a Spring JVM container, and we can
      test the application end to end by hitting the REST handle of the project with HTTP requests.
    * But our Mockito test still needs one external dependency ie MySql database. As the whole application is
      initialised by Spring Boot, it also tries to establish a database connection with the specified url in the
      application.properties via SpringBoot's autoconfiguration.
    * Here, we ask Spring to disable the AutoConfiguration of Database configuration by specifying
      DataSourceAutoConfiguration.class under exclusion list of annotation @EnableAutoConfiguration. This won't create an
      actual database connection while booting up the application.
    * As there's no database connection, we have an unresolved dependency in Spring Context ie JdbcTemplate(Java handle
      for DB connection). Here, we make use of Mockito framework and mock the Spring bean JdbcTemplate.
      And it's done, we just create our test which hits the application directly on REST layer and check whether the test
      was successful or not. Whether our expectations were met or not.

   **Conclusion**: Here we carried out a successful standalone test, without the need of an actual MySQL database.
   So what really happened is our application was deployed on local machine(temporarily for running test) in a Spring
   container but instead of talking with the mysql db connection, our application talked with a mocked JdbcTemplate
   handler.  
   But still it wasn't a complete e2e test, as the most important part of the whole application ie connection between
   Spring Boot and MySql Database is left out.
   This is where Mockito framework lacks the ability to truly provide us with an impression of Database.

2. Using Docker for quickly creating MySql DB.

    * **Class Name: mockmvc\EmployeeControllerTest**
    * **Docker cmd: docker run --name=mysql1 -p 3306:3306 -e MYSQL_ROOT_PASSWORD=mypassword -e MYSQL_DATABASE=mysql1 -d
      mysql**
    * Here, we use docker to quickly run a MySql Database instance on our local machine **manually** prior to running
      our test.
    * The Application loads up normally and tries to establish the DB connection with our running MySQL Docker image and
      the test are run.
    * This approach seems better from previously discussed approach from an E2E test perspective as the test requests
      actually travels through Database layer as well.
    * But this approach requires manual intervention of spinning up MySQL Docker image, prior to running the test and
      thus becomes impractical. CICD is not happy at all :(.
    * And then the docker resources also needs to be cleaned up from the machine manually. Again, CICD is not happy at
      all :(.

   **Conclusion**: Here we carried out a successful test, with an actual MySQL database(though deployed locally) but it
   isn't a standalone test.
   Moreover, it requires manual intervention to run/manage the database for our test.
   So, although we moved 1 step ahead from Mockito approach, a manual work was introduced into our test, and we are not
   still not happy about it.  
   Docker is a great utility for speeding up the local development, but we still cant use it directly for testing
   practices.

3. Using Testcontainers to deploy external dependency during tests

    * **Class Name: testContainers\EmployeeControllerTest**
    * Here, we make use of testcontainers to resolve our external dependency of MySQL database during the test.
    * Testcontainers solves the problem of running/managing the docker images from our previous approach.
    * Testcontainers enable us to run the external dependencies packed as a docker image during runtime through
      programmatic configuration, and the framework will manage the container in the background.
    * It sounds like a win-win for us, we can test our application e2e and all the tiresome manual work is delegated to
      the testcontainers(who is scoped only for the test runtime). Thus, our machine is cleaned with every test run and
      no manual work.
    * What Testcontainers really does is, it manages the docker images/containers in the background with the use of
      docker-java package(must be included transitive dependency with testcontainers).
    * Testcontainers are cleaned up after test scope is finished. But the test does not finish gracefully then the
      testcontainer are cleaned up using Ryuk containers. Ryuk containers are used for resource clean up by
      testcontainers and are running along with our custom docker image.
    * We need the following dependencies to be included in the project to make use of testcontainers and a running
      docker daemon on machine.
      ```
      <dependency>
			\\\    <groupId>org.testcontainers</groupId>
			\\\    <artifactId>testcontainers</artifactId>
			\\\    <version>1.17.3</version>
			\\\    <scope>test</scope>
		\\\</dependency>
      
		\\\<dependency>
			\\\    <groupId>org.testcontainers</groupId>
			\\\    <artifactId>junit-jupiter</artifactId>
			\\\    <version>1.17.3</version>
			\\\    <scope>test</scope>
		\\\</dependency>
      ```
    * To make use of testcontainers, we need to annotate the test class with @Testcontainers.
    * And at the end, we need to create a Container object. We can use GenericContainer class to create a container for
      us. GenericContainer only needs the image name like shown below.
      ```
      public static GenericContainer mysql = new GenericContainer(DockerImageName.parse("mysql:latest"))
      .withEnv("MYSQL_ROOT_PASSWORD", "mypassword")
      .withEnv("MYSQL_DATABASE", "mysql1")
      .withExposedPorts(3306);
      ```
    * The above is responsible for downloading the image of mysql from Docker Hub and creating the environment variables
      required when running the specified image. We will match the Mysql credentials with one being used in
      application.properties as the testcontainer is static and will start prior to our Spring context. And as we know
      MySQL runs on port 3306 by default, we will tell the container to expose the port 3306 of mysql docker container.
    * Although the programmatic configuration looks complete with the use of GenericContainer. There are still few
      things to be taken care of:
        1. The container is still just a configured Java object, and it wont run automatically as the lifecycle of our
           GenericContainer is still not controlled by the testcontainer framework. So, we will mark our container
           object with the @Container annotation to tell JUnit to notify this field about various events in the test
           lifecycle.
        2. As we told the container to expose 3306 docker port, testcontainers will not map this docker port with the
           same localhost port. This is done for security purpose as the port might already be in use on the localhost.
           Testcontainers uses randomized ports for each container it starts, but makes it easy to obtain the actual
           port at runtime.
        3. Marking the container field **static** or **non-static** has its own significance and be used carefully. A
           non-static field will run a new docker container for each test case in the Test class while the static
           container field will create only single instance shared across all the test cases.
        4. We have marked the container field as **static** on our test class. We have done so because our Spring Boot
           application will only establish the DB connection once on startup and not for each test. Thus, we need to
           share the same DB instance for all the tests in the class.
    * As the container is deployed on a randomised port, this makes it difficult for us to connect with testcontainer.
      But we can use the instance method `getFirstMappedPort()` to get the localhost port which is currently binded with
      the exposed docker port.
    * With spring boot, we can override the property configs by making use of @DynamicPropertySource annotation as shown
      below:
      ```
      @DynamicPropertySource
      public static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
          dynamicPropertyRegistry.add("spring.datasource.url", () -> "jdbc:mysql://" + mysql.getHost() + ":" + mysql.getFirstMappedPort() + "/mysql1");
      }
      ```
      Thus, our spring boot application knows how to connect with our testcontainer during application startup.

   **Conclusion**: Here we carried out a successful standalone test, with an actual MySQL database as a docker managed
   container by testcontainers framework.
   We configured it all programmatically with our test according to our use case, and we also don't have to manage the
   resource cleanup activities.
   So, we moved 1 step ahead from our previous Manual Docker approach and I feel quite happy about it.   
   There is one thing though, it might have been slipped while we were busy testing, but we did one manual thing there. Remember
   we had to match the credentials on GenericContainer object as environment variable.
   The tests shouldn't really depend upon the credentials as our goal is not to test the credentials here. But it's not
   really a big thing, because as a starter, this is completely in our control(plus we have achieved much more than
   previous approach :) ).

4. Using Readymade MySQLContainer Testcontainer to deploy external dependency of MySQL DB during tests

    * **Class Name: testContainers\EmployeeControllerMySQLTest**
    * Here, we also make use of testcontainers to resolve our external dependency of MySQL database during the test.
    * But as our challenge of external MySQL dependency is a common use case. Testcontainers framework have a pre-built
      class specially for using MySQL database.
    * This special class uses lesser and lesser configuration than GenericContainer and makes our time more worthy for
      development.
    * We need the following dependencies to be included in the project to make use of MySQL testcontainers
      ```
      <dependency>
           \\\\    <groupId>org.testcontainers</groupId>
           \\\\    <artifactId>mysql</artifactId>
           \\\\    <version>1.17.3</version>
           \\\\    <scope>test</scope>
       \\\\\</dependency>
      ```
    * Instead of creating a GenericContainer object, we simply use MySQLContainer with an optional parameter of docker
      image name in the constructor like below and this is all you need to spin up MySQL DB.
      ```
      @Container 
      public static MySQLContainer mysql = new MySQLContainer(DockerImageName.parse("mysql:latest"));
      ```
    * As we are using MySQLContainer, in addition to fetch running port `getFirstMappedPort()`, we can make use of mysql
      specific instance methods to get more information of our running instance like below:
      ```
       @DynamicPropertySource
      public static void overrideProps(DynamicPropertyRegistry dynamicPropertyRegistry) {
          dynamicPropertyRegistry.add("spring.datasource.url", mysql::getJdbcUrl);
          dynamicPropertyRegistry.add("spring.datasource.username", mysql::getUsername);
          dynamicPropertyRegistry.add("spring.datasource.password", mysql::getPassword); 
      }
      ```
    * Instead of providing the credentials to the testcontainer, we let the testcontainer decide them on their own and
      injected the same credentials into our Spring Application.
    * Thus, our spring boot application knows how to connect with our testcontainer during application startup again :).

   **Conclusion**: Here we carried out a successful standalone test, with an actual MySQL database as a docker managed
   container by testcontainers framework.
   We configured it all programmatically with our test according to our use case, and we also don't have to manage the
   resource cleanup activities.
   Moreover, we didn't need to hardcode the matching credentials from our Spring Context to running MySQL testcontainer
   and delegated one more configuration to testcontainers.
   So, we moved 1 step ahead from our previous Testcontainers approach(we were lucky our use case was a common one
   though ;) )
   The external dependency is not really our concern now.

5. Going one step ahead by using on-the-fly image creation of our Spring Boot Application and performing true block-box
   testing

    * **Class Name: testContainers\EmployeeControllerNetworkTest**
    * All this time, we were running our application as a spring boot container on our local machine, and it worked
      really great.
    * But as we are using testcontainers/docker, we can go one step beyond and instead of running our spring context on
      local machine, we can run it inside a separate docker container by building the local docker image on the fly of
      our application.
    * As the application is deployed on docker image, it will be truly a block box integration test.
    * This is exciting, because if we are progressing though some development work on the application. We don't have to
      wait for CICD processes to create the docker image for us and we can test our integration tests on our local
      machine immediately.
    * Moreover, as we are using Docker via testcontainers, we can run the application irrespective of the Host OS as the
      docker image also encapsulates the whole environment for us including the required OS for the application.
    * For our project, the Dockerfile content looks like below. Pretty simple, right. Just copies the jar created and
      executes the java command to run the application inside the container.
      ```
      FROM adoptopenjdk/openjdk8
      COPY /target/*.jar /usr/local/lib/targetApp.jar
      EXPOSE 8080
      ENTRYPOINT ["java","-jar","/usr/local/lib/targetApp.jar"]
      ```
    * We still need our MySQL testcontainer dependency running and thats not a real concern for us now. We have already
      solved that problem with our previous approach.
    * The problem would be letting the AppContainer somehow connect with the MySQLContainer because both are running as
      separate docker containers and are only exposed on some randomised localhost port of our machine.
    * Thus, containers don't have any direct way to communicate with each other. Their only way seems to be going through
      localhost machine everytime.(App -> localhost -> mysql)
    * Docker provides the ability for you to create custom networks and place containers on one or more networks. Then,
      communication can occur between networked containers without the need of exposing ports through the host. With
      Testcontainers, you can do this as well.
    * We can achieve this networking by creating Network object and injecting the same object on all the containers
      needed to be on same network as shown below:
      ```
      static Network network = Network.newNetwork(); 
      
      @Container
      public static MySQLContainer mysql = (MySQLContainer) new MySQLContainer(DockerImageName.parse("mysql:latest"))
      .withDatabaseName("mysql1")
      .withPassword("mypassword")
      .withNetwork(network)
      .withNetworkAliases("mysql", "mysqlserver"); //think of network aliases like a local DNS entry in Docker to a container 
      
      @Container
      public static GenericContainer app = new GenericContainer(new ImageFromDockerfile()
      .withFileFromFile("Dockerfile", new File("./Dockerfile"))
      .withFileFromFile("target", new File("./target")))
      .dependsOn(mysql)
      .withExposedPorts(8080)
      .withNetwork(network)
      .withNetworkAliases("App") //think of network aliases like a local DNS entry in Docker to a container
      .withEnv("SPRING_PROFILES_ACTIVE", "network")
      .waitingFor(Wait.forHttp("/employees").forStatusCode(200));
      ```
    * Here, we have override the Spring profile on application container as we needed to use the mysql networkAlias
      inside the Spring Boot application instead of localhost(localhost of docker is itself and not host machine).
    * Thus, our SpringBoot Application Container running on separate docker container knows how to connect with our
      MySQL testcontainer during application startup.
    * And this time we have gone one step forward as we also encapsulated the environment for our application and making
      it independent of Host OS as well.

   **Conclusion**: Here we carried out a successful standalone test, with an actual MySQL database as a docker managed
   container plus our own application running on a docker container as well.
   We configured it all programmatically with our test according to our use case and we also didnt had to manage the
   resource cleanup activities.
   Moreover, the containers were able to talk to each other directly as the testcontainers helped us placing them on the same logical network.
   So, we are able to enhance our previous ready-made Testcontainers approach to truly containerized tests independent of Host OS(as long as the Docker daemon is running on the HOST OS).

At the end, I found testcontainer framework really cool. It does help us on our tests when an external dependency is required for the application. 
We tried a lot of things here, but there are still many features uncovered on testcontainers(like controlling the lifecycle of container manually).
The testcontainers are dependent on Docker Daemon, so if one is using Docker engine remotely or as a DinD/DooD. Then working with testcontainers might be tedious but still possible.
Do checkout if you found this useful https://www.testcontainers.org/