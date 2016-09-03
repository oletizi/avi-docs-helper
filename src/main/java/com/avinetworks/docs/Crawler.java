package com.avinetworks.docs;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {
  @Override
  public boolean shouldVisit(final Page referringPage, final WebURL url) {
    // info("shouldVisit: referringPage: " + referringPage + ", url: " + url);
    String href = url.getURL().toLowerCase();
    return href.contains("https://kb.avinetworks.com/");// && ! href.contains("wp-content");
  }

  @Override
  public void visit(final Page page) {
    info("visit: " + page.getWebURL());
  }

  private final void info(final String msg) {
    System.out.println(getClass().getSimpleName() + ": " + msg);
  }

  public static void main(String[] args) throws Exception {
    String crawlStorageFolder = "/tmp/crawler/";
    int numberOfCrawlers = 1;

    CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Instantiate the controller for this crawl.
         */
    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
    controller.addSeed("https://kb.avinetworks.com/");

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
    controller.start(Crawler.class, numberOfCrawlers);

  }

}
