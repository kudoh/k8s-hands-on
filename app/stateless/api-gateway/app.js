var express = require('express');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var cors = require('cors')

var gatewayRouter = require('./routes/gateway');

var app = express();

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(cors());

app.use('/api/v1', gatewayRouter);

app.use('/health', (req, res) => {
  res.status(200).send();
});

module.exports = app;
