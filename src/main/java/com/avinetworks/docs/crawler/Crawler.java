package com.avinetworks.docs.crawler;

import com.avinetworks.docs.content.ContentConverter;
import com.avinetworks.docs.content.MarkdownConverter;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Crawler extends WebCrawler {
  private static String HOSTNAME = "kb.avinetworks.com";
  private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
  private final File outputDir;
  private ContentConverter converter;
  private Filter filter;

  Crawler(final Filter filter, final File outputDir, final ContentConverter converter) {
    this.filter = filter;
    this.outputDir = outputDir;
    this.converter = converter;
    try {
      FileUtils.forceMkdir(outputDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean shouldVisit(final Page referringPage, final WebURL url) {
    return filter.filter(url);
  }

  @Override
  public void visit(final Page page) {
    info("visit: " + page.getWebURL());
    String path = page.getWebURL().getPath();
    info("  path: " + path);
    final File outDir = new File(outputDir, path);
    try {
      FileUtils.forceMkdir(outDir);
      final String contentType = page.getContentType();
      info("  content type: " + contentType);
      if (contentType.contains("html")) {
        info("  converting content to string with charset: " + page.getContentCharset());
        String content = new String(page.getContentData(), page.getContentCharset());
        info("  received content.");
        converter.convert(page.getWebURL().toString(), content, outDir);

      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

  }

  private void info(final String msg) {
    logger.info(getClass().getSimpleName() + ": " + msg);
  }

  private static final boolean DEBUG = true;

  public static void main(String[] args) throws Exception {
    if (args.length > 0) {
      HOSTNAME = args[0];
    }
    String crawlStorageFolder = "/tmp/crawler/";
    int numberOfCrawlers = 10;

    CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Instantiate the controller for this crawl.
         */
    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

    final String seedURL;
    final Filter filter;
    if (DEBUG) {
      // http://localhost:4000/docs/latest/configuration-guide/operations/notifications/
      seedURL = "https://" + HOSTNAME + "/avi-vantage-integration-with-safenet-network-hsm-16-2-2/";
      filter = url -> seedURL.equals(url.getURL());
    } else {
      seedURL = "https://" + HOSTNAME + "/";
      filter = new HostFilter(HOSTNAME);
    }

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
    controller.addSeed(seedURL);

        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
    final File outputDir = new File("/Users/orion/work/avi-docs/src/site/");


    controller.start(() -> {
      return new Crawler(filter, outputDir, new MarkdownConverter());
    }, numberOfCrawlers);

  }

}
