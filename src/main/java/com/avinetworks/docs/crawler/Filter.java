package com.avinetworks.docs.crawler;

import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Created by orion on 9/7/16.
 */
public interface Filter {
  boolean filter(WebURL url);
}
