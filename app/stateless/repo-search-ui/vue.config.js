const webpack = require('webpack')

module.exports = {
  devServer: {
    port: 8888,
    disableHostCheck: true,
  },
  configureWebpack: {
    plugins: [
      new webpack.DefinePlugin({
        __API_URL__: `'${process.env.API_URL}${process.env.API_PATH}'`
      })
    ]
  },
  outputDir: 'dist',
  assetsDir: 'static'
}