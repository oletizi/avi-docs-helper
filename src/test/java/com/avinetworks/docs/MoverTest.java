package com.avinetworks.docs;

import net.didion.jwnl.data.Exc;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import static org.junit.Assert.*;

public class MoverTest {

  private static final Logger logger = LoggerFactory.getLogger(MoverTest.class);

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File docroot;
  private Mover mover;
  private File sourceroot;

  @Before
  public void before() throws Exception {
    docroot = tmp.newFolder();
    URL sourceURL = ClassLoader.getSystemResource("md");
    assertNotNull(sourceURL);
    sourceroot = new File(sourceURL.getPath());
    for (File dir : sourceroot.listFiles(File::isDirectory)) {
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

    assertLinkIntegrity();
    mover = new Mover(docroot);
  }

  @After
  public void after() throws Exception {
    assertLinkIntegrity();
  }

  @Test
  public void testMain() throws Exception {
    final String src = "/servers-flapping-up-down";
    final String dest = "/moved";
    final String[] args = new String[] {src, dest};
    System.setProperty("user.dir", docroot.getAbsolutePath());

    final File srcFile = new File(docroot, src);
    final File destFile = new File(docroot, dest);
    assertTrue(srcFile.isDirectory());
    assertFalse(destFile.exists());

    Mover.main(args);

    assertFalse(srcFile.exists());
    assertTrue(destFile.exists());
  }

  @Test
  public void testMoveLog() throws Exception {
    MoveLog moveLog = mover.getMoveLog();
    assertEquals(0, moveLog.getEntries().size());

    final String src = "/servers-flapping-up-down";
    final String dest = "/moved";

    testRename(src, dest);

    assertEquals(1, moveLog.getEntries().size());
    MoveLog.MoveLogEntry entry = moveLog.getEntries().get(0);
    assertEquals(src, entry.getSrc());
    assertEquals(dest, entry.getDest());

    // make sure the move log spans Mover instantiations
    mover = new Mover(docroot);
    moveLog = mover.getMoveLog();
    assertEquals(1, moveLog.getEntries().size());

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

  private void assertLinkIntegrity() throws Exception {
    final Iterator<File> markdownFiles = FileUtils.iterateFiles(docroot, new IOFileFilter() {
      @Override
      public boolean accept(File file) {
        return accept(null, file.getName());
      }

      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".md");
      }
    }, TrueFileFilter.INSTANCE);

    while (markdownFiles.hasNext()) {
      final File md = markdownFiles.next();
      Document doc = Jsoup.parse(md, "UTF-8");
      Elements anchors = doc.select("a");
      for (Element anchor : anchors) {
        if (anchor.hasAttr("href")) {
          // this is a link anchor
          final String link = anchor.attr("href");
          if (link.startsWith("http")) {
            final URL url = new URL(link);
            fail("Implement me!");
          } else if (link.startsWith("/")) {
            // this is an absolute link
            final File target = new File(docroot, link);
            assertTrue("Target for link doesn't exist!\n  link   : " + link + "\n  target : " + target + "\n  in file: " + md, target.exists());
          } else {
            // this is a relative link
            final File target = new File(md.getParentFile(), link);
            assertTrue("Target for link (" + link + ") doesn't exist: " + target + "\n  in file: " + md, target.exists());
          }
        }
      }
    }
  }

  private void assertFileInDirectory(final File dir, final String filename) {
    final String cleanFilename = filename.replaceAll("/", "");
    assertEquals(1, dir.list((dir1, name) -> cleanFilename.equals(name)).length);
  }

}