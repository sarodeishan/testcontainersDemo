FROM adoptopenjdk/openjdk8
COPY /target/*.jar /usr/local/lib/targetApp.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/targetApp.jar"]