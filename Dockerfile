# Sử dụng Eclipse Temurin JDK (OpenJDK distribution)
FROM eclipse-temurin:17-jdk-alpine AS build

# Cài đặt Maven
RUN apk add --no-cache maven

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy pom.xml và download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Tạo user
RUN addgroup -g 1001 -S appgroup && \
  adduser -u 1001 -S appuser -G appgroup

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy JAR file
COPY --from=build /app/target/*.jar app.jar

# Thay đổi ownership
RUN chown appuser:appgroup app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]