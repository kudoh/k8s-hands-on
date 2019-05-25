var express = require('express');
var router = express.Router();
var ip = require('ip');

router.get('/', (req, res) => {
  res.status(200).send(`[app1:${ip.address()}]私の戦闘力は530000です…ですが、もちろんフルパワーであなたと戦う気はありませんからご心配なく…`);
});

module.exports = router;
