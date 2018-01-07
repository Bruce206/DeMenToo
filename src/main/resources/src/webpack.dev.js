var webpackMerge = require('webpack-merge');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var commonConfig = require('./webpack.common.js');
var path = require('path');

module.exports = webpackMerge(commonConfig, {
  devtool: 'cheap-module-eval-source-map',

  output: {
    path: path.resolve('dist'),
    publicPath: '/',
    filename: '[name].js',
    chunkFilename: '[id].chunk.js'
  },

  plugins: [
    new ExtractTextPlugin('[name].css')
  ],

  devServer: {
    historyApiFallback: true,
    port: 8008,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        secure: false,
        changeOrigin: true
      },
      '/Resources': {
        target: 'http://127.0.0.1:8080',
        secure: false,
        changeOrigin: true
      },
      '/currentUser': {
        target: 'http://127.0.0.1:8080',
        secure: false,
        changeOrigin: true
      }
    }
  }
});
