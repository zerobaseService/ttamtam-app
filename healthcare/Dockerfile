FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /src
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies || true
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /src/build/libs/*.jar app.jar
RUN mkdir -p /app/uploads/journal-images /app/logs && chown -R app:app /app
USER app
EXPOSE 8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseSerialGC -XX:MaxRAMPercentage=70 -Duser.timezone=Asia/Seoul"
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=prod"]
