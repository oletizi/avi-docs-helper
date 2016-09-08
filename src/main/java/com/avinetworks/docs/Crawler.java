package com.avinetworks.docs;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import com.pnikosis.html2markdown.HTML2Md;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

public class Crawler extends WebCrawler {
  private static final String HOSTNAME = "kbdev.avinetworks.com";
  private static final Logger logger = LoggerFactory.getLogger(Crawler.class);
  private final File outputDir;
  private Filter filter;

  Crawler(final Filter filter, final File outputDir) {
    this.filter = filter;
    this.outputDir = outputDir;
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


        final Document doc = Jsoup.parse(content);
        // Extract title
        Elements titleElement = doc.select("h1.faq-title");
        // chuck the title, now that we know what it is
        final String title = titleElement.text().replaceAll(":", "&#58;");
        titleElement.remove();

        // Extract just the article content
        Elements article = doc.select("article");
        article.select("footer").remove();
        article.select("small").remove();

        // Clean up anchors and images
        article.select("a, img").removeAttr("class").removeAttr("id");
        article.select("a").removeAttr("target");

        // Unwind the crayon gunk
        unwindCrayon(article);

        // Put referenced images in a directory local to the page
        snarfImages(article, outDir);

        // TODO:
        // - Find out why some pages don't render
        // - Deal with CLI
        // - Deal with DataScript
        // - Make a "move" script that moves pages while preserving link integrity
        // - Generalize avi-docs-snarfer to be a tools project

        final String markdown = HTML2Md.convert(article.outerHtml(), "/");
        final File outfile = new File(outDir, "index.md");
        info("  Writing content to file: " + outfile);
        final PrintWriter out = new PrintWriter(new FileWriter(outfile));
        out.println("---");
        out.println("title: " + title);
        out.println("layout: default");
        out.println("---");
        out.print(markdown);
        out.flush();
        out.close();
        info("  done snarfing.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

  }

  private void unwindCrayon(Elements article) {
    final Elements crayon = article.select("div.crayon-syntax");
    final Elements textArea = crayon.select("textarea.crayon-plain");
    final String text = textArea.text();
    crayon.tagName("pre");
    crayon.removeAttr("id");
    crayon.removeAttr("class");
    crayon.removeAttr("style");
    crayon.removeAttr("data-settings");
    crayon.html("<code class=\"language-lua\">" + text + "</code>");
  }

  private void snarfImages(Elements article, File outDir) throws IOException {
    for (Element img : article.select("img")) {
      final String src = img.attr("src");
      if (src.startsWith("/wp-content/uploads/")) {
        final File imgDir = new File(outDir, "img");
        FileUtils.forceMkdir(imgDir);
        final String filename = FilenameUtils.getName(src);
        final URL url = new URL("https://kb.avinetworks.com" + src);
        File outFile = new File(imgDir, filename);
        info("  Snarfing image " + url + " to " + outFile);
        FileUtils.copyURLToFile(url, outFile);
        img.attr("src", "img/" + filename);
      } else {
        info("  NOT AN UPLOAD IMAGE: " + src);
      }
    }
    for (Element link : article.select("a")) {
      final String href = link.attr("href");
      if (href.startsWith("/wp-content/uploads")) {
        final String name = FilenameUtils.getName(href);
        final String rewrite = "img/" + name;
        link.attr("href", rewrite);
        link.removeAttr("rel");
        info("  rewrote img link from: " + href + " to " + link.outerHtml());
      }
    }

  }

  private void info(final String msg) {
    logger.info(getClass().getSimpleName() + ": " + msg);
  }


  private static final boolean DEBUG = false;

  public static void main(String[] args) throws Exception {
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
      seedURL = "https://" + HOSTNAME + "/virtual-service-and-pool-create-from-cli/";
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
    final File outputDir = new File("/Users/orion/work/workspace/avi-docs/src/site/");


    controller.start(() -> {
      return new Crawler(filter, outputDir);
    }, numberOfCrawlers);

  }

}
