# 构建阶段
FROM openjdk:21 AS builder

# 设置工作目录
WORKDIR /app

# 复制项目文件到构建镜像
COPY . .

# 授予 gradlew 脚本执行权限
RUN chmod +x ./gradlew

# 使用 Gradle Wrapper 构建项目，并禁用文件系统监视功能
RUN ./gradlew clean build --no-daemon -Dorg.gradle.vfs.watch=false -x test

# 使用更小的运行时基础镜像
FROM openjdk:21-slim

# 设置工作目录
WORKDIR /app

# 将构建好的 JAR 文件复制到运行时镜像
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 暴露 8080 端口
EXPOSE 8080

# 运行应用程序
CMD ["java", "-jar", "/app/app.jar"]