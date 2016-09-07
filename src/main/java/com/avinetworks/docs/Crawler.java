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
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

public class Crawler extends WebCrawler {
  //private final String outputDir = "/tmp/doc2md";
  private final File outputDir;

  public Crawler() {
    outputDir = new File("/tmp/avi-docs/src/site/");
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
    return href.contains("https://kb.avinetworks.com/")
        && !href.contains("kb.avinetworks.com/wp-content/")
        && !href.contains("kb.avinetworks.com/wp-json/")
        && !href.contains("kb.avinetworks.com/tag/")
        && !href.contains("kb.avinetworks.com/author/")
        && !href.contains("kb.avinetworks.com/category/")
        ;
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
        String content = new String(page.getContentData(), page.getContentCharset());

        final Document doc = Jsoup.parse(content);
        Elements titleElement = doc.select("h1.faq-title");
        // chuck the title, now that we know what it is
        final String title = titleElement.text();
        titleElement.remove();
        // extract title: h1.faq-title
        // content: main.main
        Elements article = doc.select("article");
        article.select("footer").remove();
        article.select("small").remove();

        article.select("a, img").removeAttr("class").removeAttr("id");
        article.select("a").removeAttr("target");

        snarfImages(article, outDir);

        // TODO:
        // - Deal with tables
        // - Find out why some pages don't render
        // - Deal with CLI
        // - Deal with DataScript
        // - Make a "move" script that moves pages while preserving link integrity
        // - Generalize avi-docs-snarfer to be a tools project

        final String markdown = HTML2Md.convert(article.outerHtml(), "/");
        final File outfile = new File(outDir, "index.md");
        info("Writing content to file: " + outfile);
        final PrintWriter out = new PrintWriter(new FileWriter(outfile));
        out.println("---");
        out.println("title: " + title);
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
