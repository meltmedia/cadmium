fs            = require 'fs'
{print}       = require 'util'
{spawn, exec} = require 'child_process'

# Task Definition
# ---------------

task 'prepare', 'Prepare the site for first run', ->
  run 'rm', ['-rf', './node_modules'], ->
    run 'npm', ['install'], callback?

task 'clean', 'Cleanup generated content', ->
  run './node_modules/.bin/docpad', ['clean'], ->
    invoke 'prepare'

option '-g', '--github', 'Depoy the generated docs to github pages for `docs` task'
task 'docs', 'Generate annotated source code with Groc', (options) ->
  params = if options.github then ["--github"] else []
  run './node_modules/.bin/groc', params, callback?

task 'run', 'Start site in development mode', ->
  run './node_modules/.bin/docpad', ['run'], callback?

task 'deploy', 'Deploy to specified environemtn', (options) ->
  callback?

task 'test', 'Run the test suite', ->
  test callback?


# Utility functions
# -----------------

# Run a generic command on the underlying OS, terminating on any exit code > 0
run = (command, options, callback) ->
  if typeof options is 'function'
    callback = options
    options = []
  options or= []
  proc = spawn command, options
  proc.stdout.on 'data', (data) -> print data.toString()
  proc.stderr.on 'data', (data) -> process.stderr.write(data)
  proc.on 'exit', (status) ->
    if 0 isnt status
      print "`#{command}` exited with status #{status}"
      return process.exit(status)
    callback?()

# Start a server, run test suite, then stop server
test = (callback) ->
  testStarted = false
  run './node_modules/.bin/docpad', ['clean'], () ->
    run './node_modules/.bin/docpad', ['generate'], () ->
      server = spawn './node_modules/.bin/docpad', ['server', '-p', '9778'] # Start server

      server.stderr.on 'data', (data) -> process.stderr.write(data)
      server.stdout.on 'data', (data) ->
        print data.toString()

        # Only start testing once DocPad says it's listening
        if data.toString().match(/The action completed successfully/) and not testStarted
          testStarted = true

          # Start tests
          tests = spawn './node_modules/.bin/mocha'
          tests.stdout.on 'data', (data) -> print data.toString()
          tests.stderr.on 'data', (data) -> process.stderr.write(data)
          tests.on 'exit', (status) ->
            server.kill()
            if 0 isnt status
              return process.exit(status)
            callback?()
