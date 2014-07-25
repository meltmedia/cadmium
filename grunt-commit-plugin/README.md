# grunt-commit-plugin

> Grunt plugin to deploy a cadmium site.

## Getting Started
This plugin requires Grunt `~0.4.5`

If you haven't used [Grunt](http://gruntjs.com/) before, be sure to check out the [Getting Started](http://gruntjs.com/getting-started) guide, as it explains how to create a [Gruntfile](http://gruntjs.com/sample-gruntfile) as well as install and use Grunt plugins. Once you're familiar with that process, you may install this plugin with this command:

```shell
npm install grunt-commit-plugin --save-dev
```

Once the plugin has been installed, it may be enabled inside your Gruntfile with this line of JavaScript:

```js
grunt.loadNpmTasks('grunt-commit-plugin');
```

## The "cadmium_commit" task

### Overview
In your project's Gruntfile, add a section named `cadmium_commit` to the data object passed into `grunt.initConfig()`.

```js
grunt.initConfig({
  cadmium_commit: {
    domain: '',     
    out: '',     
    message: '', 
    cwd: '',    
    useSSL: true 
  }
});
```

### Options

#### options.domain
Type: `String`
Required: true

The domain to deploy the content to.

#### options.out
Type: `String`
Default value: `'out'`

The directory to deploy.

#### options.message
Type: `String`
Default value: `'Deployed by grunt-commit-plugin'`

The message that will be used as both a commit message for the content repository and the message in the audit history of cadmium.

#### options.cwd
Type: `String`
Default value: `'.'`

The working directory for this plugin the out path will be relative to this.

#### options.useSSL
Type: `boolean`
Default value: `true`

Tells the plugin to use https calls to the cadmium sites api.

## Contributing
In lieu of a formal styleguide, take care to maintain the existing coding style. Add unit tests for any new or changed functionality. Lint and test your code using [Grunt](http://gruntjs.com/).

## Release History
_(Nothing yet)_
