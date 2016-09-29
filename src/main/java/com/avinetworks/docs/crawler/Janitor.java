package com.avinetworks.docs.crawler;

import com.avinetworks.docs.content.ContentConverter;
import com.avinetworks.docs.content.MarkdownConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clean up after all the snarfing.
 */
public class Janitor {

  private static final Logger logger = LoggerFactory.getLogger(Janitor.class);

  private final File docroot;
  private final String url;
  private ContentConverter converter;

  public Janitor(final File docroot, final String url, final ContentConverter converter) {
    this.docroot = docroot;
    this.url = url;
    this.converter = converter;
  }

  public void cleanup() {
    try {
      URL canonicalUrl = new URL(this.url);
      HttpResponse<String> response = Unirest.get(this.url).asString();
      if (response.getStatus() == 200) {
        final Document doc = Jsoup.parse(response.getBody());
        for (Element link : doc.select("a")) {
          if (link.hasAttr("href")) {
            String href = link.attr("href");
            info("href: " + href);
            if (href.startsWith("/")) {
              // check to see if the link works
              String checkUrl = canonicalUrl.getProtocol() + "://" + canonicalUrl.getHost();
              if (canonicalUrl.getPort() > 0) {
                checkUrl += ":" + canonicalUrl.getPort();
              }
              checkUrl += href;
              HttpResponse<String> checkResponse = Unirest.get(checkUrl).asString();
              if (checkResponse.getStatus() == 404) {
                // try to snarf the content from the kb site
                final String snarfUrl = "https://kb.avinetworks.com" + href;
                info("Broken link: " + checkUrl + "; snarfing from: " + snarfUrl);
                final File outDir = new File(docroot, href);
                final HttpResponse<String> snarfResponse = Unirest.get(snarfUrl).asString();
                if (snarfResponse.getStatus() == 200) {
                  try {
                    converter.convert(snarfUrl, snarfResponse.getBody(), outDir);
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                } else {
                  info("Snarf url fail: status: " + snarfResponse.getStatus());
                }

              } else if (checkResponse.getStatus() == 200) {
              } else {
                info("Who knows? Status: " + checkResponse.getStatus() + ": " + checkUrl);
              }
            }
          }
        }
      } else {
        throw new RuntimeException("Can't reach URL: " + this.url);
      }
    } catch (UnirestException | MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private void info(String s) {
    logger.info(s);
  }

  public static void main(final String[] args) {
    final List<String> urls = new ArrayList();
    urls.addAll(Arrays.asList(args));
    if (urls.size() <= 0) {
      urls.add("http://docs.avinetworks.com/");
    }
    final String docroot = System.getProperty("docroot") != null ? System.getProperty("docroot") : "/Users/orion/work/workspace/avi-docs/src/site/";
    for (String arg : urls) {
      new Janitor(new File(docroot), arg, new MarkdownConverter()).cleanup();
    }
  }
}
