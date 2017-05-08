package com.avinetworks.docs.content;

import com.pnikosis.html2markdown.HTML2Md;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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


public class MarkdownConverter implements ContentConverter {

  private static final Logger logger = LoggerFactory.getLogger(MarkdownConverter.class);

  public void convert(String url, String content, File outDir) throws IOException {
    final Document doc = Jsoup.parse(content);
    // Extract title
    Elements titleElement = doc.select("h1.faq-title");
    // chuck the title, now that we know what it is
    final String title = titleElement.text().replaceAll(":", "&#58;").replaceAll("\\.", "&#46;");
    titleElement.remove();

    // Extract just the article content
    Elements article = doc.select("article");
    article.select("footer").remove();
    article.select("small").remove();

    // Clean up anchors and images
    article.select("a").removeAttr("class").removeAttr("id").removeAttr("target");

    // Unwind the crayon gunk
    unwindCrayon(article);

    // Put referenced images in a directory local to the page
    snarfImages(article, outDir);

    String markdown = HTML2Md.convert(article.outerHtml(), "/");

    markdown = new MarkdownCleaner().clean(markdown);
    String rv = "---\n" +
        "title: " + title + "\n" +
        "layout: default\n" +
        "---\n" +
        markdown;
    final File outfile = new File(outDir, "index.md");
    FileUtils.forceMkdir(outDir);
    info("  Writing content to file: " + outfile);
    final PrintWriter out = new PrintWriter(new FileWriter(outfile));
    out.print(rv);
    out.flush();
    out.close();
    info("  done snarfing.");
  }

  private void unwindCrayon(Elements article) {
    final Elements crayon = article.select("div.crayon-syntax");
    for (Element element : crayon) {
      final Elements textArea = element.select("textarea.crayon-plain");
      final String text = textArea.text();
      element.tagName("pre");
      element.removeAttr("id");
      element.removeAttr("class");
      element.removeAttr("style");
      element.removeAttr("data-settings");
      element.html("<code class=\"language-lua\">" + text + "</code>");
    }
  }

  private void snarfImages(Elements article, File outDir) throws IOException {
    for (Element img : article.select("img")) {
      final String src = img.attr("src");
      if (src.startsWith("/wp-content/uploads/")) {
        final File imgDir = new File(outDir, "img");
        FileUtils.forceMkdir(imgDir);
        final String filename = FilenameUtils.getName(src);
        final URL url = new URL("https://kbstage.avinetworks.com" + src);
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
}
