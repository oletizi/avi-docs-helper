package com.avinetworks.docs.crawler;

import com.avinetworks.docs.content.MarkdownConverter;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Snarfer {

  public static void main(final String args[]) throws Exception {
    final File snarfSpec = new File(ClassLoader.getSystemResource("modified-posts.csv").getPath());

    System.out.println("Snarf spec file: " + snarfSpec);

    final File outputDir = new File("/Users/orion/work/avi-docs/src/site/docs/17.1-changes");
    final String crawlStorageFolder = "/tmp/crawler/";
    final CrawlController controller;
    final int numberOfCrawlers = 1;

    final CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder(crawlStorageFolder);

        /*
         * Instantiate the controller for this crawl.
         */
    final PageFetcher pageFetcher = new PageFetcher(config);
    final RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    final RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    controller = new CrawlController(config, pageFetcher, robotstxtServer);

    final Set<String> urls = new HashSet<>();

    final Reader reader = new FileReader(snarfSpec);
    final CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL);
    parser.forEach(strings -> {
      System.out.println(strings.get(0));
      final String seedURL = "https://kb.avinetworks.com/" + strings.get(0) + "/";
      urls.add(seedURL);
      try {
        controller.addSeed(seedURL);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    final Filter filter = url -> urls.contains(url.getURL());
    controller.start(() -> new Crawler(filter, outputDir, new MarkdownConverter()), numberOfCrawlers);
  }
}
