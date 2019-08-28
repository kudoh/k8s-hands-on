const express = require('express');
const Redis = require('ioredis');
const webClient = require('./webClient');
const axios = require('axios');
const JSONCache = require('redis-json');
const router = express.Router();
const { check, validationResult } = require('express-validator/check');

const redisPort = process.env.REDIS_PORT || 6379
const redisHost = process.env.REDIS_HOST || 'localhost'
const redisPassword = process.env.REDIS_PASSWORD || null

const redis = new Redis({
  port: redisPort,
  host: redisHost,
  password: redisPassword
});
 
const jsonCache = new JSONCache(redis, {prefix: 'cache:'});

router.get('/repos', check('query').not().isEmpty(), checkValidationResult, async (req, res, next) => {

  console.log(`received request. serach parameter: ${req.query.query}`);

  const query = req.query.query;

  const response = await jsonCache.get(query);

  if (response) {
    console.log(`retrieved from Cache. query: ${query}`)  
    res.status(200).json(response);
    return;
  }

  console.log("cache not found. consult to github-service...")
  webClient(req).get(`/repos?query=${query}`)
    .then(async result => {
      console.log(`retrieved github data successfully. caching for next request.`)
      if (result.data.length === 0) {
        res.status(200).json([]);
      } else {
        await jsonCache.set(query, result.data, {expire: 60 * 60});
        res.status(200).json(result.data);
      }
    })
    .catch(e => {
      console.error(e);
      res.status(500).send(e.message);
    });
});

function checkValidationResult(req, res, next) {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }
  next();
}

// for Maintenance
router.post('/clearCache', (req, res) => {
  jsonCache.clearAll();
  res.status(200).end();
})

module.exports = router;
