FROM openjdk:17
VOLUME /tmp
EXPOSE 8002
ADD ./target/ms-concentrador-energia-0.0.1-SNAPSHOT.jar servicio-concentrador-energia.jar
ENTRYPOINT ["java", "-jar", "servicio-concentrador-energia.jar"]
