'use strict';

var grunt = require('grunt');
var asyncblock = require('asyncblock');
var exec = require('child_process').exec;

/*
  ======== A Handy Little Nodeunit Reference ========
  https://github.com/caolan/nodeunit

  Test methods:
    test.expect(numAssertions)
    test.done()
  Test assertions:
    test.ok(value, [message])
    test.equal(actual, expected, [message])
    test.notEqual(actual, expected, [message])
    test.deepEqual(actual, expected, [message])
    test.notDeepEqual(actual, expected, [message])
    test.strictEqual(actual, expected, [message])
    test.notStrictEqual(actual, expected, [message])
    test.throws(block, [error], [message])
    test.doesNotThrow(block, [error], [message])
    test.ifError(value)
*/

var run = function(cmds, callback) {
  asyncblock(function(flow) {
    for(var i in cmds) {
      grunt.log.writeln(cmds[i].cmd);
      exec(cmds[i].cmd, {cwd: cmds[i].cwd, stdio: 'inherit'}, flow.add());
      flow.wait();
    }
    callback();
  });
};

exports.commit_test = {
  commit: function(test) {
    test.expect(6);

    run([
      {cmd:'git clone repo.git', cwd:'target'},
      {cmd:'git checkout cd-master', cwd:'target/repo'}
    ], function() {
      test.ok(grunt.config.get('spys.status').isDone(), 'No status called.');
      test.ok(grunt.config.get('spys.update').isDone(), 'No update sent.');
      test.ok(grunt.config.get('spys.history').isDone(), 'No history wait.');

      test.ok(!grunt.file.exists('target/repo/README.md'), 'old content still exists.');
      test.ok(grunt.file.exists('target/repo/index.html'), 'No files checked in.');
      test.ok(grunt.file.exists('target/repo/META-INF/source'), 'No source file added.');

      test.done();
    });
  }
};
