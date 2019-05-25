FROM node:12.3.1-alpine as builder

# avoid running nodejs app as PID 1
RUN apk add --no-cache tini

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm install

COPY . .

# switch user node:node for runAsNonRoot
USER 1000:1000

EXPOSE 3000

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["node", "./bin/www"]
