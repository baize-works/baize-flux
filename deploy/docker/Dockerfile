# Build first: mvn -pl baize-flux-launcher -am package
FROM eclipse-temurin:8-jre
ARG BAIZE_FLUX_VERSION=1.0.0
WORKDIR /opt
COPY baize-flux-launcher/target/baize-flux-${BAIZE_FLUX_VERSION}.tar.gz /tmp/baize-flux.tar.gz
RUN tar -xzf /tmp/baize-flux.tar.gz -C /opt && mv /opt/baize-flux-${BAIZE_FLUX_VERSION} /opt/baize-flux && rm /tmp/baize-flux.tar.gz \
    && useradd --system --uid 10001 --create-home baizeflux \
    && mkdir -p /opt/baize-flux/logs && chown -R baizeflux:baizeflux /opt/baize-flux
USER baizeflux
ENV BAIZE_FLUX_HOME=/opt/baize-flux
WORKDIR /opt/baize-flux
ENTRYPOINT ["/opt/baize-flux/bin/baize-flux.sh"]
CMD ["--config", "/opt/baize-flux/config/baize-flux.yaml"]
