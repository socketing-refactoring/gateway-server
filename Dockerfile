FROM bellsoft/liberica-openjdk-alpine:21
WORKDIR /app
COPY ./build/libs/gateway-server.jar /app/gateway-server.jar
ENTRYPOINT ["java", "-jar", "gateway-server.jar"]
