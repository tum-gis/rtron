# adopted from: https://github.com/holgerbrandl/kscript/blob/master/misc/Dockerfile

FROM ubuntu:latest

MAINTAINER benedikt.schwab@tum.de

WORKDIR '/app'

SHELL ["/bin/bash", "-c"]

RUN apt-get update && \
	apt-get -y install curl zip unzip && \
    rm -rf /var/lib/apt/lists/*

# Install https://sdkman.io
RUN curl -s "https://get.sdkman.io" | bash

RUN source /root/.sdkman/bin/sdkman-init.sh && \
    sdk install java 11.0.12-zulu && \
    sdk install kotlin 1.5.21 && \
    sdk install kscript 3.1.0

ENTRYPOINT source /root/.sdkman/bin/sdkman-init.sh && /usr/bin/env kscript "$0" "$@"
CMD [ "--help" ]
