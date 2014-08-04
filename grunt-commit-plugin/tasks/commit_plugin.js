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
var path = require('path');
var trim = require('trim');
var http = require('http');
var https = require('https');
var os = require('os');
var fs = require('fs');

module.exports = function(grunt) {

  var Commit = function(options) {
    this.options = options;

    this.options.site = /(?:http[s]?\:\/\/)?([^\/]+)\/?/.exec(this.options.site)[1];
    this.http = (this.options.ssl ? https : http);
  };

  var rm = function(file) {
    if(grunt.file.isDir(file)) {
      var dirContents = fs.readdirSync(file);
      for(var i in dirContents) {
        rm(path.join(file, dirContents[i]));
      }
      fs.rmdirSync(file);
    } else {
      fs.unlinkSync(file);
    }
  };

  var readStream = function(stream, callback) {
    var buffer = '';
    stream.on('data', function(chunk) {
      buffer += chunk;
    });

    stream.on('end', function() {
      callback(buffer, null);
    });

    stream.on('error', function() {
      callback(null, 'Failed to read response from server.');
    });
  };

  var getHomeDir = function() {
    return process.env.HOME || process.env.HOMEPATH || process.env.USERPROFILE;
  };

  var makeRequestOptions = function($this, path, method, hasContent) {
    var reqOptions = {
      hostname: $this.options.site,
      path:     path,
      headers:  {
        Authorization: $this.apiToken
      }
    };

    if($this.options.ssl) {
      reqOptions.rejectUnauthorized = false;
    }

    if(method) {
      reqOptions.method = method;
    }

    if(hasContent) {
      reqOptions.headers['Content-Type'] = 'application/json';
    }

    return reqOptions;
  };

  // Runs async functions in order.
  Commit.prototype.run = function(cmds, callback) {
    this.cmds = cmds;
    var $this = this;
    asyncblock(function(flow) {
      $this.flow = flow;
      for(var i = 0; i < cmds.length; i++) {
        grunt.log.debug('Running ' + cmds[i]);
        $this[cmds[i]](flow.add());
        flow.wait();
      }
      callback();
    });
  };

  // Determines if the build is on CI.
  Commit.prototype.checkForGitRemote = function(callback) {
    var $this = this;
    exec('git fetch', {cwd: this.options.cwd, stdio: 'inherit'}, function (error) {
      if(error !== null) {
        $this.hasRemote = false;
      } else {
        $this.hasRemote = true;
      }

      grunt.log.ok('Has remote: ' + $this.hasRemote);

      callback();
    });
  };

  // Fails build if repo has outstanding changes.
  Commit.prototype.checkDirty = function(callback) {
    if(this.hasRemote) {
      exec('git status -s --untracked-files=no', {cwd: this.options.cwd}, function(error, stdout, stderr) {
        if(trim(stdout) !== '' || trim(stderr) !== '') {
          callback("Please commit or stash changes before deploying.");
          return;
        } else {
          callback();
        }
      });
    } else {
      callback();
    }
  };

  // Sets this.remote
  Commit.prototype.getRemoteRepo = function(callback) {
    if(this.hasRemote) {
      var $this = this;
      exec('git config --get remote.origin.url', {cwd: this.options.cwd}, function(error, stdout, stderr) {
        if(error === null) {
          $this.remote = trim(stdout);
          grunt.log.ok('Git Remote: ' + $this.remote);
          callback();
        } else {
          $this.remote = 'bamboo';
          callback('Unknown remote');
        }
      });
    } else {
      this.remote = 'bamboo';
      grunt.log.ok('Git Remote: ' + this.remote);
      callback();
    }
  };

  // Sets this.branch
  Commit.prototype.getBranch = function(callback) {
    var $this = this;
    exec('git symbolic-ref HEAD', {cwd: this.options.cwd}, function(error, stdout, stderr) {
      if(error === null) {
        $this.branch = trim(stdout).split('/').pop();
        grunt.log.ok('Git Branch: ' + $this.branch);
        callback();
      } else {
        callback('Unable to get the source repositories branch.');
      }
    });
  };

  // Gets the current revision for a git repository.
  Commit.prototype.gitRevision = function(repoPath, callback) {
    exec('git rev-parse HEAD', {cwd: repoPath}, function(error, stdout, stderr) {
      if(error === null) {
        callback(trim(stdout));
      } else {
        grunt.log.warn('STDOUT: ' + stdout);
        grunt.log.warn('STDERR: ' + stderr);
        callback(null);
      }
    });
  };

  // Sets this.revision
  Commit.prototype.getSourceRevision = function(callback) {
    var $this = this;
    this.gitRevision(this.options.cwd, function(rev) {
      if(rev !== null) {
        $this.revision = rev;
        grunt.log.ok('Git Revision: ' + $this.revision);
        callback();
      } else {
        callback('Unable to get the current revision for the source repository.');
      }
    });
  };

  // Creates a META-INF/source file in the content to tie content to the current commit.
  Commit.prototype.getSource = function(callback) {
    this.source = {
      repo:   this.remote,
      sha:    this.revision,
      branch: this.branch
    };
    var metaDir = path.join(this.options.out, 'META-INF');
    if(!grunt.file.exists(metaDir)) {
      grunt.file.mkdir(metaDir);
    }
    var sourceFile = path.join(metaDir, 'source');
    if(grunt.file.exists(sourceFile)) {
      grunt.file.delete(sourceFile);
    }
    grunt.file.write(sourceFile, JSON.stringify(this.source));
    callback();
  };
  

  // Get api token
  Commit.prototype.getApiToken = function(callback) {
    this.apiToken = trim(grunt.file.read(path.join(getHomeDir(), '.cadmium', 'github.token')));
    grunt.log.debug('Using api token: ' + this.apiToken);
    callback();
  };

  // Get status of remote site and set this.status object.
  Commit.prototype.getStatus = function(callback) {
    grunt.log.ok('Getting status for [' + this.options.site + ']');
    var $this = this;
    this.http.request(makeRequestOptions(this, '/system/status'), function(res) {
      if(res.statusCode === 200) {
        readStream(res, function(data, error) {
          if(error !== null) {
            callback(error);
          } else {
            grunt.log.debug('Status returned: ' + data);
            $this.status = JSON.parse(data);
            var remoteName = /([^\/]+)\.git/.exec($this.status.repo)[1];
            var cloneDir = path.join(os.tmpdir(), 'cloned-remotes', remoteName + '_' + $this.status.branch);
            
            $this.clonedRemoteDirectory = cloneDir;
            callback();
          }
        });
      } else {
        callback('Status returned ' + res.statusCode);
      }
    }).on('error', function(e) {
      callback(e);
    }).end();
  };

  // Removes old temporary cloned directory.
  Commit.prototype.removeTemp = function(callback) {
    if(grunt.file.exists(this.clonedRemoteDirectory)) {
      rm(this.clonedRemoteDirectory);
    }
    callback();
  };

  // Clones remote repository into tmp directory.
  Commit.prototype.cloneRemote = function(callback) {
    grunt.log.debug('Cloning to directory: ' + this.clonedRemoteDirectory);
    var $this = this;
    
    var postCloneCallback = function(error, stdout, stderr) {
      if(error === null) {
        grunt.log.debug('Cloned remote to ' + $this.clonedRemoteDirectory);
        callback();
      } else {
        grunt.log.warn('STDOUT: ' + stdout);
        grunt.log.warn('STDERR: ' + stderr);
        callback('Failed to remove changes.');
      }
    };

    if(!grunt.file.exists(this.clonedRemoteDirectory)) {
      exec('git clone --branch ' + this.status.branch + ' ' + this.status.repo + ' ' + this.clonedRemoteDirectory, {cwd: this.options.out}, postCloneCallback);
    }
  };

  // Removes old content.
  Commit.prototype.rmOldContent = function(callback) {
    exec('git rm -r *', {cwd: this.clonedRemoteDirectory}, function(error, stdout, stderr) {
      if(error === null) {
        callback();
      } else {
        grunt.log.warn('STDOUT: ' + stdout);
        grunt.log.warn('STDERR: ' + stderr);
        callback('Failed to remove changes.');
      }
    });
  };

  
  // Adds changed files to content repo.
  Commit.prototype.addChanged = function(callback) {
    var $this = this;
    grunt.file.recurse(this.options.out, function(abspath, rootdir, subdir, filename) {
      grunt.file.copy(abspath, [$this.clonedRemoteDirectory, subdir, filename].join('/'));
    });
    exec('git add -A', {cwd: this.clonedRemoteDirectory}, function(error, stdout, stderr) {
      if(error === null) {
        callback();
      } else {
        grunt.log.warn('STDOUT: ' + stdout);
        grunt.log.warn('STDERR: ' + stderr);
        callback('Failed to add new content.');
      }
    });
  };
  
  // Commit changes to content repo.
  Commit.prototype.commitChanges = function(callback) {
    exec('git commit -m "' + this.options.message + '"', {cwd: this.clonedRemoteDirectory}, function(error, stdout, stderr) {
      if(error !== null) {
        grunt.log.warn('STDOUT: ' + stdout);
        grunt.log.warn('STDERR: ' + stderr);
      }
      callback();
    });
  };
  
  // Push changes to remote.
  Commit.prototype.pushChanges = function(callback) {
    exec('git push origin ' + this.status.branch, {cwd: this.clonedRemoteDirectory}, function(error, stdout, stderr) {
      if(error === null) {
        callback();
      } else {
        grunt.log.warn('STDOUT: ' + stdout);
        grunt.log.warn('STDERR: ' + stderr);
        callback('Failed to push new content to repository.');
      }
    });
  };
  
  // Get new revision.
  Commit.prototype.getContentRevision = function(callback) {
    var $this = this;
    this.gitRevision(this.clonedRemoteDirectory, function(rev) {
      if(rev !== null) {
        $this.newRev = rev;
        callback();
      } else {
        callback('Unable to get the current revision for the content repository.');
      }
    });
  };

  // Send update message to remote site.
  Commit.prototype.sendUpdate = function(callback) {
    var $this = this;
    var req = this.http.request(makeRequestOptions(this, '/system/update', 'POST', true), function(res) {
      if(res.statusCode === 200) {
        readStream(res, function(data, error) {
          if(error !== null) {
            callback(error);
          } else {
            $this.update = JSON.parse(data);
            grunt.log.ok('Updating content on ' + $this.options.site);
            callback();
          }
        });
      } else {
        callback('Failed to send update message.');
      }
    });

    req.write(JSON.stringify({
      sha:     this.newRev,
      comment: this.options.message
    }));
    req.end();
  };

  // Waits for site to finish updating.
  Commit.prototype.waitForDone = function(callback) {
    var $this = this;
    if(typeof this.update.uuid === 'string') {
      this.http.request({hostname: this.options.site, path: '/system/history/' + this.update.uuid, rejectUnauthorized: false}, function(res) {
        if(res.statusCode === 200) {
          readStream(res, function(data, error) {
            if(error !== null) {
              callback(error);
            } else {
              if(data === 'true') {
                grunt.log.ok('Finished updating.');
              } else {
                $this.cmds.push('waitForDone');
              }
              callback();
            }
          });
        } else {
          callback('Deployment failed!');
        }
      }).end();
    } else {
      grunt.log.ok('Committed to old version of cadmium. Your changes will be finished deploying in a few minutes.');
      callback();
    }
  };
  

  // Runs all logic for the commit.
  Commit.prototype.runCommit = function() {
    grunt.log.writeln('Deploying ' + this.options.out + ' to ' + this.options.site);

    this.run([
      'checkForGitRemote',
      'checkDirty',
      'getRemoteRepo',
      'getBranch',
      'getSourceRevision',
      'getSource',
      'getApiToken',
      'getStatus',
      'removeTemp',
      'cloneRemote',
      'rmOldContent',
      'addChanged',
      'commitChanges',
      'pushChanges',
      'getContentRevision',
      'removeTemp',
      'sendUpdate',
      'waitForDone'
    ], this.options.callback);
  };

  grunt.registerTask('cadmium_commit', 'Grunt plugin to deploy a cadmium site.', function() {
    // Require a site url in the configuration.
    var done = this.async();
    grunt.config.requires('cadmium_commit.domain');

    var siteUrl = grunt.config('cadmium_commit.domain');
    var outDir  = grunt.config('cadmium_commit.out');
    var message = grunt.config('cadmium_commit.message');
    var cwdVal = grunt.config('cadmium_commit.cwd');
    var useSSL = grunt.config('cadmium_commit.useSSL');
    if(typeof cwdVal !== 'string') {
      cwdVal = '.';
    } 
    if(typeof outDir !== 'string') {
      outDir = 'out';
    } 
    if(typeof message !== 'string') {
      message = 'Deployed by grunt-commit-plugin';
    }
    if(typeof useSSL === 'undefined') {
      useSSL = true;
    }
    var commit = new Commit({
      callback: done,
      site: siteUrl,
      out: path.join(cwdVal, outDir),
      cwd: cwdVal,
      ssl: useSSL,
      message: message
    });

    if(!grunt.file.exists(commit.options.out)) {
      grunt.log.warn('Cannot commit content from [' + commit.options.out + ']: directory doesn\'t exist!')
    } else {
      commit.runCommit();
    }

  });

};
