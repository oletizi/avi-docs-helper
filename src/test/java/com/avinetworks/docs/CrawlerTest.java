package com.avinetworks.docs;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for simple Crawler.
 */
public class CrawlerTest {

  private static final Logger logger = LoggerFactory.getLogger(CrawlerTest.class);
  private Server server;

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  @After
  public void after() {
    if (server != null) {
      server.stop();
      server = null;
    }
  }

  @org.junit.Test
  public void testServer() throws Exception {
    String expectedContent = "Hello!";
    int port = startServer(expectedContent);

    final String servedContent = urlToString(new URL("http://localhost:" + port + "/"));
    logger.info("Served content: " + servedContent);
    assertEquals(expectedContent, servedContent);
  }

  private String urlToString(final URL url) throws IOException {
    File tmpFile = tmp.newFile();
    FileUtils.copyURLToFile(url, tmpFile);
    return FileUtils.readFileToString(tmpFile, "UTF-8");
  }

  @org.junit.Test
  public void testTable() throws Exception {
    final String content = urlToString(ClassLoader.getSystemResource("html/table-and-datascript.html"));
    assertNotNull(content);

    // start the embedded webserver
    int port = startServer(content);
    assertTrue(port > 0);
    final Filter filter = mock(Filter.class);

    String snarfUrl = "http://localhost:" + port + "/";

    // make sure the server is serving what we expect
    assertEquals(content, urlToString(new URL(snarfUrl)));

    // create a filter that returns true for the snarfUrl
    WebURL webURL = new WebURL();
    webURL.setURL(snarfUrl);
    when(filter.filter(webURL)).thenReturn(true);
    assertTrue(filter.filter(webURL));


    final File outDir = tmp.newFolder();
    final Crawler crawler = new Crawler(filter, outDir);
    CrawlConfig config = new CrawlConfig();
    config.setCrawlStorageFolder(tmp.newFolder().getAbsolutePath());
    PageFetcher pageFetcher = new PageFetcher(config);
    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

    controller.addSeed(snarfUrl);

    //  Start the crawl. This is a blocking operation, meaning that your code
    //  will reach the line after this only when crawling is finished.


    controller.start(new WebCrawlerFactory<Crawler>() {
      public Crawler newInstance() throws Exception {
        return crawler;
      }
    }, 1);

    String[] outFiles = outDir.list();
    assertEquals(1, outFiles.length);
    File markdown = new File(outDir, outFiles[0]);

    Document doc = Jsoup.parse(FileUtils.readFileToString(markdown, "UTF-8"));
    Elements table = doc.select("table");
    assertTrue(table.size() > 0);
  }

  private int startServer(final String content) throws IOException {
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();
    serverSocket.close();
    server = new Server(port, content);
    return port;
  }
}
