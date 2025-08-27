FROM eclipse-temurin:17-jre
COPY target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app.jar"]