/*
 * grunt-commit-plugin
 * https://github.com/meltmedia/cadmium
 *
 * Copyright (c) 2014 John McEntire
 * Licensed under the MIT license.
 */

'use strict';

var asyncblock = require('asyncblock');
var exec = require('child_process').exec;
var nock = require('nock');
var path = require('path');

module.exports = function(grunt) {

  var run = function(cmds, done) {
    asyncblock(function(flow) {
      try {
        for(var i in cmds) {
          grunt.log.debug(cmds[i].cmd);
          exec(cmds[i].cmd, {cwd: cmds[i].cwd, stdio: 'inherit'}, flow.add());
          flow.wait();
        }
        done();
      } catch(e) {
        grunt.log.warn('Failed to setup: ' + e.message);
        done(false);
      }
    });
  };

  // Project configuration.
  grunt.initConfig({
    jshint: {
      all: [
        'Gruntfile.js',
        'tasks/*.js',
        '<%= nodeunit.tests %>'
      ],
      options: {
        jshintrc: '.jshintrc'
      }
    },

    // Unit tests.
    nodeunit: {
      tests: ['test/*_test.js']
    },

    cadmium_commit: {
      domain: 'cadmium-test.com',
      outDir: 'out',
      cwd: 'target'
    },

    clean: {
      files: ['target', 'target.git']
    },

    spys: {}

  });

  // Actually load this plugin's task(s).
  grunt.loadTasks('tasks');

  // These plugins provide necessary tasks.
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-nodeunit');

  // Register task to create directory to commit
  grunt.registerTask('setup', 'Creates test directory', function() {
    var done = this.async();
    grunt.log.ok('Creating test out directory...');
    grunt.file.mkdir('target.git');
    grunt.file.mkdir('target');
    grunt.file.write('target/README.md', 'Test');
    grunt.file.mkdir('target/out');
    grunt.log.ok('Populating test out directory...');
    grunt.file.recurse('./test/fixtures/out', function(abspath, rootdir, subdir, filename) {
      grunt.file.copy(abspath, ['target/out', subdir, filename].join('/'));
    });

    grunt.file.copy('./test/fixtures/gitignore', 'target/.gitignore');

    grunt.log.ok('Creating git repository...');
    grunt.file.mkdir('target/repo.git');
    grunt.file.mkdir('target/repo');
    grunt.file.write('target/repo/README.md', 'Test');
    grunt.file.write('target/repo/index.html', 'Test');
    grunt.file.mkdir('target/repo/old');
    grunt.file.write('target/repo/old/index.html', 'Test');
    run([
      // Setting up target directory to get around the need to have content committed.
      {cmd:'git init --bare', cwd: 'target.git'},
      {cmd:'git init', cwd: 'target'},
      {cmd:'git add -A', cwd: 'target'},
      {cmd:'git commit -m "initial commit"', cwd: 'target'},
      {cmd:'git remote add origin ../target.git', cwd: 'target'},
      {cmd:'git push -u origin master', cwd: 'target'},
      // Setting up test repo for content to be pushed to.
      {cmd:'git init --bare', cwd:'target/repo.git'},
      // Setting up test repo to seed test repo for tests.
      {cmd:'git init', cwd:'target/repo'},
      {cmd:'git checkout -b cd-master', cwd:'target/repo'},
      {cmd:'git add -A', cwd:'target/repo'},
      {cmd:'git commit -m "initial check in"', cwd:'target/repo'},
      {cmd:'git remote add origin ../repo.git', cwd:'target/repo'},
      {cmd:'git push -u origin cd-master', cwd:'target/repo'},
      {cmd:'rm -rf repo', cwd:'target'}
    ], done);
  });

  // Mocks out http request that will be made by commit plugin.
  grunt.registerTask('mock-http', 'Mocks out http requests.', function() {
    grunt.config.set('spys.status', nock('https://cadmium-test.com').
                        get('/system/status').
                        reply(200, {
                          repo: path.resolve('./target/repo.git'),
                          branch: 'cd-master',
                          revision: 'HEAD'
                    }));

    grunt.config.set('spys.update', nock('https://cadmium-test.com').
                        post('/system/update').
                        reply(200, {
                          message: 'ok',
                          uuid: 'testing',
                          timestamp: 1
                        }));

    grunt.config.set('spys.history', nock('https://cadmium-test.com').
                        get('/system/history/testing').
                        times(4).
                        reply(200, 'false')).
                        get('/system/history/testing').
                        reply(200, 'true');
  });

  // Whenever the "test" task is run, first clean the "tmp" dir, then run this
  // plugin's task(s), then test the result.
  grunt.registerTask('test', ['clean', 'setup', 'mock-http', 'cadmium_commit', 'nodeunit']);

  // By default, lint and run all tests.
  grunt.registerTask('default', ['jshint', 'test']);

};
