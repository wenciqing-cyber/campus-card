#!/usr/bin/env bash
# 把后端运行期需要的 jar 从本地 m2 仓库收拢到 backend/lib，
# 这样启动不依赖 Maven（本机离线，maven-jar-plugin 拉不下来）。
set -e
cd "$(dirname "$0")"
M2="${M2_REPO:-/c/Users/WCQ/.m2/repository}"
DEST=backend/lib
rm -rf "$DEST"; mkdir -p "$DEST"

JARS="
org/springframework/boot/spring-boot-starter-web/3.2.7/spring-boot-starter-web-3.2.7.jar
org/springframework/boot/spring-boot/3.2.7/spring-boot-3.2.7.jar
org/springframework/boot/spring-boot-autoconfigure/3.2.7/spring-boot-autoconfigure-3.2.7.jar
org/springframework/boot/spring-boot-starter-logging/3.2.7/spring-boot-starter-logging-3.2.7.jar
ch/qos/logback/logback-classic/1.4.14/logback-classic-1.4.14.jar
ch/qos/logback/logback-core/1.4.14/logback-core-1.4.14.jar
org/apache/logging/log4j/log4j-to-slf4j/2.21.1/log4j-to-slf4j-2.21.1.jar
org/apache/logging/log4j/log4j-api/2.21.1/log4j-api-2.21.1.jar
org/slf4j/jul-to-slf4j/2.0.13/jul-to-slf4j-2.0.13.jar
org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar
jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar
org/yaml/snakeyaml/2.2/snakeyaml-2.2.jar
org/springframework/boot/spring-boot-starter-json/3.2.7/spring-boot-starter-json-3.2.7.jar
com/fasterxml/jackson/core/jackson-databind/2.15.4/jackson-databind-2.15.4.jar
com/fasterxml/jackson/core/jackson-annotations/2.15.4/jackson-annotations-2.15.4.jar
com/fasterxml/jackson/core/jackson-core/2.15.4/jackson-core-2.15.4.jar
com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.15.4/jackson-datatype-jdk8-2.15.4.jar
com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.15.4/jackson-datatype-jsr310-2.15.4.jar
com/fasterxml/jackson/module/jackson-module-parameter-names/2.15.4/jackson-module-parameter-names-2.15.4.jar
org/springframework/boot/spring-boot-starter-tomcat/3.2.7/spring-boot-starter-tomcat-3.2.7.jar
org/apache/tomcat/embed/tomcat-embed-core/10.1.25/tomcat-embed-core-10.1.25.jar
org/apache/tomcat/embed/tomcat-embed-websocket/10.1.25/tomcat-embed-websocket-10.1.25.jar
org/apache/tomcat/embed/tomcat-embed-el/10.1.25/tomcat-embed-el-10.1.25.jar
org/springframework/spring-web/6.1.10/spring-web-6.1.10.jar
org/springframework/spring-webmvc/6.1.10/spring-webmvc-6.1.10.jar
org/springframework/spring-beans/6.1.10/spring-beans-6.1.10.jar
org/springframework/spring-aop/6.1.10/spring-aop-6.1.10.jar
org/springframework/spring-context/6.1.10/spring-context-6.1.10.jar
org/springframework/spring-expression/6.1.10/spring-expression-6.1.10.jar
org/springframework/spring-core/6.1.10/spring-core-6.1.10.jar
org/springframework/spring-jcl/6.1.10/spring-jcl-6.1.10.jar
io/micrometer/micrometer-observation/1.12.7/micrometer-observation-1.12.7.jar
io/micrometer/micrometer-commons/1.12.7/micrometer-commons-1.12.7.jar
org/springframework/boot/spring-boot-starter-validation/3.2.7/spring-boot-starter-validation-3.2.7.jar
org/hibernate/validator/hibernate-validator/8.0.1.Final/hibernate-validator-8.0.1.Final.jar
jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar
org/jboss/logging/jboss-logging/3.5.3.Final/jboss-logging-3.5.3.Final.jar
com/fasterxml/classmate/1.6.0/classmate-1.6.0.jar
org/springframework/boot/spring-boot-starter-jdbc/3.2.7/spring-boot-starter-jdbc-3.2.7.jar
com/zaxxer/HikariCP/5.0.1/HikariCP-5.0.1.jar
org/springframework/spring-jdbc/6.1.10/spring-jdbc-6.1.10.jar
org/springframework/spring-tx/6.1.10/spring-tx-6.1.10.jar
org/mybatis/mybatis/3.5.16/mybatis-3.5.16.jar
org/mybatis/mybatis-spring/3.0.3/mybatis-spring-3.0.3.jar
com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar
"

for p in $JARS; do
  if [ -f "$M2/$p" ]; then cp "$M2/$p" "$DEST/"; else echo "MISS $p"; fi
done

echo "lib jars: $(ls "$DEST" | wc -l)"
