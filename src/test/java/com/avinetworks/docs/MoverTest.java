package com.avinetworks.docs;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class MoverTest {

  private static final Logger logger = LoggerFactory.getLogger(MoverTest.class);

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File docroot;
  private Mover mover;

  @Before
  public void before() throws Exception {
    docroot = tmp.newFolder();
    URL sourceURL = ClassLoader.getSystemResource("md");
    assertNotNull(sourceURL);
    File sourceDir = new File(sourceURL.getPath());
    for (File dir : sourceDir.listFiles(File::isDirectory)) {
      File dest = new File(docroot, dir.getName());
      FileUtils.forceMkdir(dest);
      FileUtils.copyDirectory(dir, dest);
    }

    assertTrue(docroot.list().length > 0);
    for (File dir : docroot.listFiles()) {
      assertTrue(dir.isDirectory());
      assertTrue(dir.list().length > 0);
      // ensure there is one and only one index.md directory
      final String filename = "index.md";
      assertFileInDirectory(dir, filename);
    }
    mover = new Mover(docroot);
  }

  private void assertFileInDirectory(final File dir, final String filename) {
    final String cleanFilename = filename.replaceAll("/", "");
    logger.info("Checking for " + filename + " in " + dir);
    for (String s : dir.list()) {
      logger.info("  found: " + s);
    }

    assertEquals(1, dir.list((dir1, name) -> cleanFilename.equals(name)).length);
  }

  @Test
  public void testMoveToNewDirectory() throws Exception {
    // test move directory to another directory
    String source = "/manually-validate-server-health";
    String dest = "/docs/";

    File sourceDir = new File(docroot, source);
    File destDir = new File(docroot, dest);

    assertTrue(sourceDir.isDirectory());
    assertFalse(destDir.exists());
    mover.move(source, dest);
    assertFalse(sourceDir.exists());
    assertTrue(destDir.exists());

    assertFileInDirectory(destDir, source);
  }

  @Test
  public void testRenameAbsolute() throws Exception {
    testRename("/servers-flapping-up-down", "/moved");
  }

  @Test
  public void testRenameRelative() throws Exception {
    testRename("servers-flapping-up-down", "moved");
  }

  private void testRename(String source, String dest) throws IOException {
    File sourceDir = new File(docroot, source);
    File destDir = new File(docroot, dest);
    logger.info("Source dir: " + sourceDir);
    logger.info("Dest file: " + destDir);
    assertTrue("Not a directory: " + sourceDir, sourceDir.isDirectory());
    assertFalse(destDir.exists());
    mover.move(source, dest);
    assertFalse(sourceDir.exists());
    assertTrue(destDir.isDirectory());

    assertFileInDirectory(destDir, "index.md");
    assertFileInDirectory(destDir, "img");

    File imgDir = new File(destDir, "img");
    assertFileInDirectory(imgDir, "HealthMonitor2.png");
  }
}