FROM node:11.10.0-alpine

RUN apk add --update --no-cache curl tini

ENV APP_DIR=/usr/local/app
RUN mkdir -p ${APP_DIR}
WORKDIR ${APP_DIR}
COPY . ${APP_DIR}

HEALTHCHECK --interval=5s \
            --timeout=5s \
            --retries=6 \
            CMD curl --fail http://localhost:3000/health || exit 1

# switch user node:node for runAsNonRoot
USER 1000:1000

EXPOSE 3000
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["node", "./bin/www"]
