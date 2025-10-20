FROM tomcat:9.0-jdk16

# 패키지 목록을 업데이트하고, FFmpeg을 설치.
# UTF-8 로케일 설정
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Java 인코딩 설정
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

# 패키지 목록을 업데이트하고, FFmpeg과 로케일 패키지를 설치.
# -y 플래그는 설치 과정의 모든 질문에 'Yes'로 답하도록 설정.
RUN apt-get update && apt-get install -y ffmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg locales && \
    locale-gen en_US.UTF-8 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 기존 default webapps 삭제 (필수 아님, 권장)
RUN rm -rf /usr/local/tomcat/webapps/*

# Tomcat server.xml에서 UTF-8 인코딩 설정
RUN sed -i 's/<Connector port="8080"/<Connector port="8080" URIEncoding="UTF-8"/' /usr/local/tomcat/conf/server.xml

# WAR 복사 (Gradle 빌드 결과물)
COPY build/libs/*.war /usr/local/tomcat/webapps/ROOT.war