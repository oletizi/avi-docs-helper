package com.avinetworks.docs.crawler;

import edu.uci.ics.crawler4j.url.WebURL;

public class HostFilter implements Filter {
  private String hostname;

  public HostFilter(final String hostname) {
    this.hostname = hostname;
  }

  public boolean filter(WebURL url) {
    String href = url.getURL().toLowerCase();
    return href.contains("https://" + hostname + "/")
        && !href.contains(hostname + "/wp-content/")
        && !href.contains(hostname + "/wp-json/")
        && !href.contains(hostname + "/tag/")
        && !href.contains(hostname + "/author/")
        && !href.contains(hostname + "/category/")
        && !href.contains(hostname + "/page/")
        && !href.contains(hostname + "/wp-login")
        && !href.endsWith(".png")
        && !href.endsWith(".gif")
        && !href.endsWith(".jpg")
        && !href.endsWith(".jpeg")
        ;

  }
}
