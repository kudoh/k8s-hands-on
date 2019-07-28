const express = require('express');
const Redis = require('ioredis');
const axios = require('axios');
const JSONCache = require('redis-json');
const router = express.Router();
const { check, validationResult } = require('express-validator/check');

const serviceURL = process.env.SERVICE_URL || 'http://localhost:8080/github/'
const redisPort = process.env.REDIS_PORT || 6379
const redisMaster = process.env.REDIS_HOST_MASTER || 'localhost'
const redisSlave = process.env.REDIS_HOST_SLAVE || 'localhost'
const redisPassword = process.env.REDIS_PASSWORD || null

// Redi Clusterを使えばRead/Writeで分離しなくていい
// https://github.com/luin/ioredis/issues/387
const master = new Redis({
    port: redisPort,
    host: redisMaster,
    password: redisPassword
});
const slave = new Redis({
    port: redisPort,
    host: redisSlave,
    password: redisPassword
});
 
const jsonWriteCache = new JSONCache(master, {prefix: 'cache:'});
const jsonReadCache = new JSONCache(slave, {prefix: 'cache:'});

const client = axios.create({
  baseURL: serviceURL,
  timeout: 5000
});

router.get('/repos', check('query').not().isEmpty(), checkValidationResult, async (req, res, next) => {

  const query = req.query.query;
  console.log(`received request. serach parameter: ${query}`);

  const response = await jsonReadCache.get(query);

  if (response) {
    console.log(`retrieved from Cache. query: ${query}`)  
    res.status(200).json(response);
    return;
  }

  console.log("cache not found. consult to github-service...")
  client.get(`/repos?query=${query}`)
    .then(async result => {
      console.log(`retrieved github data successfully. caching for next request.`)
      if (result.data.length === 0) {
        res.status(200).json([]);
      } else {
        await jsonWriteCache.set(query, result.data, {expire: 60 * 60});
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
  jsonWriteCache.clearAll();
  res.status(200).end();
})

module.exports = router;
