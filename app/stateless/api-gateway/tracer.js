const initTracerFromEnv = require('jaeger-client').initTracerFromEnv;
const appName = require('./package').name;
const appVersion = require('./package').version;

// See schema https://github.com/jaegertracing/jaeger-client-node/blob/master/src/configuration.js
const jaegerConfig = {serviceName: appName};

const options = {
  tags: {}
};
options.tags[appName + '.version'] = appVersion;

const tracer = initTracerFromEnv(jaegerConfig, options);

module.exports = tracer;
