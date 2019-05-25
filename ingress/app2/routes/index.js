var express = require('express');
var router = express.Router();
var ip = require('ip');

router.get('/', (req, res) => {
  res.status(200).send(`[app2:${ip.address()}]ずいぶんムダな努力をするんですね・・・そんなことがわたしに通用するわけがないでしょう！`);
});

module.exports = router;
