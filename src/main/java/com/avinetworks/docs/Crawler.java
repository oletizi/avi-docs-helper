package com.avinetworks.docs;

import com.pnikosis.html2markdown.HTML2Md;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Crawler extends WebCrawler {
  //private final String outputDir = "/tmp/doc2md";
  private final File outputDir;

  public Crawler() {
    outputDir = new File("/tmp/doc2md");
    try {
      FileUtils.forceMkdir(outputDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean shouldVisit(final Page referringPage, final WebURL url) {
    // info("shouldVisit: referringPage: " + referringPage + ", url: " + url);
    String href = url.getURL().toLowerCase();
    return href.contains("https://kb.avinetworks.com/");// && ! href.contains("wp-content");
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
        info("  converting content to string....");
        final String content = new String(page.getContentData(), page.getContentCharset());
        final String markdown = HTML2Md.convert(content, "/");
        final File outfile = new File(outDir, "index.md");
        info("Writing content to file: " + outfile);
        final PrintWriter out = new PrintWriter(new FileWriter(outfile));
        out.println("---");
        out.println("title: I'm the Title");
        out.println("layout: default");
        out.println("---");
        out.print(markdown);
        out.flush();
        out.close();
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

  }

  private void info(final String msg) {
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
