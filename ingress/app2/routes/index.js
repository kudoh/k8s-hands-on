var express = require('express');
var router = express.Router();

router.get('/', (req, res) => {
  res.status(200).send('[app2]ずいぶんムダな努力をするんですね・・・そんなことがわたしに通用するわけがないでしょう！');
});

module.exports = router;
