var Crawler = require('simplecrawler').Crawler,
    util = require('util'),
    AssertionError = require('assert').AssertionError;

describe('Broken link checker', function() {

  it('should crawl site and return 0 broken links', function(done) {

    this.timeout(30000); // ensure enough time to crawl the site

    var brokenLinks = [],
        siteCrawler = new Crawler("localhost", "/", 9778);

    // configure crawler
    siteCrawler.downloadUnsupported = false;
    siteCrawler.supportedMimeTypes = [
        /^text\//i,
        /^application\/(rss)?[\+\/\-]?xml/i,
        /^xml/i
      ];

    // handle broken links
    siteCrawler.on("fetch404", function(queueItem, responseBuffer, response) {
      brokenLinks.push(queueItem);
    });

    // on completion, parse broken links and report
    siteCrawler.on("complete", function(queueItem, responseBuffer, response) {
      if (brokenLinks.length > 0) {
        var msg = util.format("Expected 0 broken links, found %s\n".grey, brokenLinks.length);
        brokenLinks.forEach(function(item) {
          msg += util.format("\t- broken link: ".red + "%s".yellow + " on page: ".red + "%s\n".yellow, item.url, item.referrer);
        });
        throw new AssertionError({ message: msg, actual: brokenLinks.length, expected: 0 });
      }
      done();
    });

    siteCrawler.start(); // crawl the site

  });
  
});

