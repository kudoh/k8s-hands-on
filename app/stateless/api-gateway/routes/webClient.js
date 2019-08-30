const axios = require('axios');
const { Tags, FORMAT_HTTP_HEADERS } = require('opentracing');
const serviceURL = process.env.SERVICE_URL || 'http://localhost:8080/github/'

/**
 * Web Client with OpenTracing.
 */
module.exports = function webClient(req) {

  const parentSpan = req.span;
  const client = axios.create({
      baseURL: serviceURL,  
      timeout: 5000
    });

  // request hook
  client.interceptors.request.use(config => {
    console.log(`intercepting request for ${config.url}`);
    const tracer = parentSpan.tracer();
    const span = tracer.startSpan(config.method.toUpperCase(), {
      childOf: parentSpan.context(),
      tags: {[Tags.SPAN_KIND]: Tags.SPAN_KIND_RPC_CLIENT}
    });
    try {
      span.setTag(Tags.HTTP_URL, config.url);
      span.setTag(Tags.HTTP_METHOD, config.method);
      span.log({event: 'calling_webapi', url: config.url, method: config.method});
      // Send span context via request headers (parent id etc.)
      span.tracer().inject(span, FORMAT_HTTP_HEADERS, config.headers);
      config.__span = span;
    } catch (err) {
      span.log({event: 'error', err: err.message});
      span.finish();
      throw err;
    }
    return config;
  }, error => {
    if (error.config.__span) {
      const currentSpan = error.config.__span;
      if (currentSpan) {
        currentSpan.setTag(Tags.ERROR, true);
        currentSpan.log({event: 'request_error_webapi'});
        currentSpan.finish();
      }
    }
    console.error(`error for ${config.url} ${error}`);
    return Promise.reject(error);
  });

  // response hook
  client.interceptors.response.use(response => {
    const currentSpan = response.config.__span;
    currentSpan.setTag(Tags.HTTP_STATUS_CODE, response.status);
    currentSpan.log({event: 'complete_calling_webapi', status: response.status});
    currentSpan.finish();
    return response;
  }, error => {
    const currentSpan = error.config.__span;
    currentSpan.setTag(Tags.ERROR, true);
    if (error.response) {
      currentSpan.setTag(Tags.HTTP_STATUS_CODE, error.response.status || 500);
      currentSpan.log({event: 'response_error_webapi', error: error.response.data});
    }
    currentSpan.finish();
    return Promise.reject(error);
  });

  return client;
};
