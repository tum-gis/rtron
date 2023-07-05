FROM gradle:jdk17 AS builder

LABEL maintainer="Benedikt Schwab"
LABEL maintainer.email="benedikt.schwab(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/tum-gis/rtron"

COPY . /home/app
WORKDIR /home/app

RUN gradle shadowJar


FROM bellsoft/liberica-openjdk-debian:17 AS runtime

WORKDIR /app
COPY --from=builder /home/app/rtron-cli/build/libs/rtron-1*.jar /app/app.jar

# ENTRYPOINT ["java", "-jar", "/app/app.jar"]
WORKDIR /
COPY entrypoint.sh /entrypoint.sh
RUN ["chmod", "+x", "entrypoint.sh"]
