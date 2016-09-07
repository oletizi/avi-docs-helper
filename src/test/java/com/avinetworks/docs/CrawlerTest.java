package com.avinetworks.docs;

import edu.uci.ics.crawler4j.url.WebURL;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.aspectj.lang.annotation.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

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
    File tmpFile = tmp.newFile();
    FileUtils.copyURLToFile(new URL("http://localhost:" + port + "/"), tmpFile);
    String servedContent = FileUtils.readFileToString(tmpFile, "UTF-8");
    System.out.println("Served content: " + servedContent);
    assertEquals(expectedContent, servedContent);
  }

  @org.junit.Test
  public void testTable() throws Exception {
    final URL url = ClassLoader.getSystemResource("html/table-and-datascript.html");
    String content = FileUtils.readFileToString(new File(url.getPath()), "UTF-8");

    int port = startServer(content);
    assertTrue(port > 0);
    assertNotNull(url);
    final Filter filter = mock(Filter.class);
    WebURL webURL = new WebURL();
    webURL.setURL(url.toString());

    when(filter.filter(webURL)).thenReturn(true);

    Crawler crawler = new Crawler(filter);


  }

  private int startServer(final String content) throws IOException {
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();
    serverSocket.close();
    server = new Server(port, content);
    return port;
  }
}
