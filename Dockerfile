####
# This Dockerfile is used in order to build a container that runs the Quarkus application in JVM mode
#
# Before building the container image run:
#
# ./mvnw package
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile.jvm -t jflygare/pilsnerk8s:1.2.4-SNAPSHOT --format docker .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 jflygare/pilsnerk8s:1.2.4-SNAPSHOT
#

FROM gcr.io/distroless/java21-debian13

WORKDIR /app

COPY target/quarkus-app/ /app/


#EXPOSE 8080
#ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
#ENV JAVA_APP_JAR="/app/quarkus-run.jar"

CMD ["quarkus-run.jar"]

