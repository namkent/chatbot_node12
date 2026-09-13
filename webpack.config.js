const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const VueLoaderPlugin = require('vue-loader/lib/plugin');

module.exports = (env = {}, options = {}) => {
  const isProd = options.mode === 'production';

  return {
    mode: isProd ? 'production' : 'development',
    entry: './src/main.js',
    output: {
      path: path.resolve(__dirname, 'dist'),
      filename: 'bundle.[hash:8].js',
      publicPath: '/'
    },
    resolve: {
      extensions: ['.js', '.vue', '.json', '.scss'],
      alias: {
        vue$: 'vue/dist/vue.esm.js',
        '@': path.resolve(__dirname, 'src')
      }
    },
    module: {
      noParse: /vendor[\\\/]mermaid\.js/,
      rules: [
        {
          test: /\.vue$/,
          loader: 'vue-loader'
        },
        {
          test: /\.js$/,
          loader: 'babel-loader',
          exclude: [/node_modules[\\\/](?!mermaid)/, /src[\\\/]vendor/],
          options: {
            presets: ['@babel/preset-env']
          }
        },
        {
          test: /\.scss$/,
          use: [
            'vue-style-loader',
            { loader: 'css-loader', options: { sourceMap: !isProd } },
            { loader: 'sass-loader', options: { sourceMap: !isProd } }
          ]
        },
        {
          test: /\.css$/,
          use: ['vue-style-loader', 'css-loader']
        },
        {
          test: /\.(png|jpe?g|gif|svg|webp)(\?.*)?$/,
          loader: 'file-loader',
          options: {
            name: 'assets/img/[name].[hash:8].[ext]',
            esModule: false
          }
        },
        {
          test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
          loader: 'file-loader',
          options: {
            name: 'assets/fonts/[name].[hash:8].[ext]',
            esModule: false
          }
        }
      ]
    },
    plugins: [
      new VueLoaderPlugin(),
      new HtmlWebpackPlugin({
        template: './index.html',
        filename: 'index.html',
        inject: true
      })
    ],
    devServer: {
      host: '127.0.0.1',
      port: 3002,
      hot: true,
      open: false,
      historyApiFallback: true,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:3001',
          changeOrigin: true
        }
      }
    },
    devtool: isProd ? false : 'cheap-module-eval-source-map'
  };
};
