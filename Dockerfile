# 使用 Eclipse Temurin OpenJDK 17 镜像（更稳定）
FROM eclipse-temurin:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 复制 Maven 构建的可执行 JAR 文件到容器中
COPY target/tool-testing-demo-1.0-SNAPSHOT.jar app.jar

# 创建用于存储上传文件的目录
RUN mkdir -p /uploads

# 暴露应用端口
EXPOSE 8090

# 设置 JVM 参数以优化容器环境中的性能
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
