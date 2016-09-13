package com.avinetworks.docs;

public class MarkdownCleaner {
  public String clean(String markdown) {
    markdown = markdown.replaceAll("&nbsp;", " ");
    return markdown;
  }
}
