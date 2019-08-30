const express = require('express');
const cookieParser = require('cookie-parser');
const logger = require('morgan');
const cors = require('cors')
const tracer = require('./tracer')
const { Tags, FORMAT_HTTP_HEADERS } = require('opentracing');
const url = require('url');

const gatewayRouter = require('./routes/gateway');

const app = express();

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(cors());

app.use('/health', (req, res) => {
  res.status(200).send();
});

// middleware for Distributed Tracing
app.use((req, res, next) => {

  const parentSpanContext = tracer.extract(FORMAT_HTTP_HEADERS, req.headers);
  
  const span = tracer.startSpan(url.parse(req.url).pathname, {
    childOf: parentSpanContext,
    tags: {[Tags.SPAN_KIND]: Tags.SPAN_KIND_RPC_SERVER}
  });
  span.setTag(Tags.COMPONENT, 'nodejs');
  span.log({ event: 'received_request', method:req.method, url: req.url});
  console.log(`accepting... : ${req.method} ${req.url}`)

  req.span = span;

  next();

  res.on('finish', () => {
    console.log(`returning response ${res.statusCode}`)
    if (req.span) {
      req.span.log({ event: 'complete_request_processing', status: res.statusCode});
      if (res.statusCode >= 400) {
        req.span.setTag(Tags.ERROR, true);
      }
      req.span.finish();
    }
  });
})

app.use('/api/v1', gatewayRouter);

module.exports = app;
