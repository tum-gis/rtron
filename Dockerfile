# adopted from: https://github.com/holgerbrandl/kscript/blob/master/misc/Dockerfile

FROM openjdk:11.0.12-jdk-buster

LABEL maintainer="Benedikt Schwab"
LABEL maintainer.email="benedikt.schwab(at)tum.de"
LABEL maintainer.organization="Chair of Geoinformatics, Technical University of Munich (TUM)"
LABEL source.repo="https://github.com/tum-gis/rtron"

WORKDIR '/project'

SHELL ["/bin/bash", "-c"]

RUN apt-get update && \
	apt-get -y install curl zip unzip && \
    rm -rf /var/lib/apt/lists/*

# Install https://sdkman.io
RUN curl -s "https://get.sdkman.io" | bash && \
    source /root/.sdkman/bin/sdkman-init.sh && \
    # sdk install java 11.0.12-zulu && \
    sdk install kotlin 1.5.21 && \
    sdk install kscript 3.1.0

# Download rtron dependency
ARG RTRON_VERSION="1.2.2"
ENV RTRON_VERSION=$RTRON_VERSION
RUN source /root/.sdkman/bin/sdkman-init.sh && /usr/bin/env kscript "@file:DependsOn(\"io.rtron:rtron-main:$RTRON_VERSION\")"

ENTRYPOINT source /root/.sdkman/bin/sdkman-init.sh && /usr/bin/env kscript "$0" "$@"
CMD [ "--help" ]
