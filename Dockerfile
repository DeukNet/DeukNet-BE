FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 애플리케이션 실행 유저 추가 (보안)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 미리 빌드된 JAR 파일 복사
COPY ./deuknet-infrastructure/build/libs/*.jar app.jar

# Actuator health endpoints 확인용 포트
EXPOSE 8080

# Spring Boot 실행
ENTRYPOINT ["java", "-jar", "app.jar"]