# 1. 자바 17 버전을 베이스로 사용
FROM eclipse-temurin:17-jdk-alpine

# 2. 컨테이너 내부에서 작업할 폴더 지정
WORKDIR /app

# 3. 빌드된 jar 파일을 컨테이너 내부로 복사
COPY build/libs/*SNAPSHOT.jar app.jar

# 4. 컨테이너가 뜰 때 실행할 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]