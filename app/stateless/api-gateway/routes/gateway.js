const express = require('express');
const axios = require('axios')
const router = express.Router();
const { check, validationResult } = require('express-validator/check');
const serviceURL = process.env.SERVICE_URL || 'http://localhost:8080/github/'

const client = axios.create({
  baseURL: serviceURL,
  timeout: 5000
});

router.get('/repos', check('query').exists(), checkValidationResult, function(req, res, next) {
  console.log(`serach parameter: ${req.query.query}`);
  client.get(`/repos?query=${req.query.query}`)
    .then(result => {
      res.status(200).json(result.data);
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

module.exports = router;
