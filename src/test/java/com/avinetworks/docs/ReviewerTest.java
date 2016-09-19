package com.avinetworks.docs;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class ReviewerTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File sourceFile;
  private File destFile;
  private Reviewer reviewer;
  private String hostA;
  private String hostB;

  @Before
  public void before() throws IOException {
    final URL sourceURL = ClassLoader.getSystemResource("reviewer/page-list.txt");
    sourceFile = new File(sourceURL.getPath());
    destFile = tmp.newFile();
    hostA = "baseUrl1";
    hostB = "baseUrl2";
    reviewer = new Reviewer(sourceFile, destFile, hostA, hostB);
  }

  @Test
  public void testBasics() throws IOException {
    reviewer.run();
    assertTrue(destFile.exists());
    assertTrue(destFile.length() > 0);
    Document doc = Jsoup.parse(destFile, "UTF-8");
    assertTrue(doc.select("a[href=http://" + hostA + "/avi-vantage-16-1-2-release-notes]").size() > 0);
    assertTrue(doc.select("a[href=http://" + hostB + "/avi-vantage-16-1-2-release-notes]").size() > 0);
    System.out.println(FileUtils.readFileToString(destFile, "UTF-8"));
  }

}