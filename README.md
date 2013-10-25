Cadmium - Getting Started Guide
===========

This site is staticly generated using DocPad and deployed into a Cadmium instance.

Make sure you are on node `v0.8.25`. This is a requirement for the project, due to the older version of DocPad that it runs.
If you are on different version, [nvm](https://github.com/creationix/nvm) helps with managing different versions of node.

```
nvm install 0.8.25
```

If you already have 0.8.25 installed:

```
nvm use 0.8.25
```

## Usage

- `cake prepare`: To prepare the site to run for the first time 
- `cake run`: To generate the site and start up a static file server at http://localhost:9778/
- `cake clean`: To clean up any previously generated content
- `cake test`: Run the test suite
- `cake docs [-g]`: To generate the documentation, optionaly uploading it to github

## Prerequisites

#### Manditory

- `brew install node`: Install Node using Homebrew [if not already installed]
- `brew install npm`: Install NPM Package Manager [if not already installed]
- `npm install -g coffee-script`: CoffeeScript

#### Optional

- `easy_install Pygments`: Pygments - Syntax highlighter used during documentation generation

## Documentation

- [Cadmium](http://meltmedia.github.com/cadmium)
- [Docpad](https://github.com/bevry/docpad/wiki)
