package com.avinetworks.docs.structure;

import com.avinetworks.docs.MarkdownCleaner;
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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MoverTest {

  private static final Logger logger = LoggerFactory.getLogger(MoverTest.class);

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File docroot;
  private Mover mover;
  private File sourceroot;
  private RedirectHandler redirectHandler;

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
    redirectHandler = mock(RedirectHandler.class);
    mover = new Mover(docroot, redirectHandler);
  }

  @After
  public void after() throws Exception {
    assertLinkIntegrity();
    assertFileIntegrity();
  }

  @Test
  public void testMain() throws Exception {
    final String src = "/servers-flapping-up-down";
    final String dest = "/moved";
    final String[] args = new String[]{src, dest};
    System.setProperty("docroot", docroot.getAbsolutePath());

    final File srcFile = new File(docroot, src);
    final File destFile = new File(docroot, dest);
    assertTrue(srcFile.isDirectory());
    assertFalse(destFile.exists());

    Mover.main(args);

    assertFalse(srcFile.exists());
    assertTrue(destFile.exists());

    // test replay
    FileUtils.moveDirectory(destFile, srcFile);
    assertTrue(srcFile.exists());
    assertFalse(destFile.exists());

    Mover.main(new String[]{ "replay" });
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
    mover = new Mover(docroot, redirectHandler);
    moveLog = mover.getMoveLog();
    assertEquals(1, moveLog.getEntries().size());

  }

  @Test
  public void testSourceDoesntExist() throws Exception {
    String source = "l;aksdjfal;";
    String dest = "/docs/";
    testRename(source, dest, false);
  }

  @Test
  public void testMoveFile() throws Exception {
    String source = "health-monitor-troubleshooting/index.md";
    String dest = "health-monitor-troubleshooting/index.moved";
    testRename(source, dest);
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

  @Test
  public void testRenameToSelf() throws Exception {
    testRename("servers-flapping-up-down", "servers-flapping-up-down", false);
  }

  @Test
  public void testRenameToExistingTarget() throws Exception {
    testRename("servers-flapping-up-down", "overview-of-health-monitors", false);
  }

  @Test
  public void testMoveToExistingTarget() throws Exception {
    testRename("servers-flapping-up-down", "overview-of-health-monitors/", false);
  }

  private void testRename(String source, String dest) throws Exception {
    testRename(source, dest, true);
  }

  private void testRename(String source, String dest, boolean assertExistenceConditions) throws IOException {
    File sourceFile = new File(docroot, source);
    File destFile = new File(docroot, dest);
    if (assertExistenceConditions) {
      assertTrue("Source doesn't exist: " + sourceFile, sourceFile.exists());
      assertFalse(destFile.exists());
    }
    final File movedTo = mover.move(source, dest);
    logger.info("Final destination:\n  source  : " + sourceFile + "\n  dest    : " + destFile + "\n  moved to: " + movedTo);
    if (assertExistenceConditions) {
      assertTrue("File moved to doesn't exist:\n  source: " + source + "\n  dest : " + dest + "\n  moved to: " + movedTo, movedTo.exists());
      assertFalse(sourceFile.exists());
      assertTrue(movedTo.exists());
    }
    if (movedTo != null && movedTo.isDirectory()) {
      assertFileInDirectory(movedTo, "index.md");
      assertFileInDirectory(movedTo, "img");

      File imgDir = new File(movedTo, "img");
      assertFileInDirectory(imgDir, "HealthMonitor2.png");
    } else if (movedTo != null) {
      assertTrue("Moved to doesn't exist: " + movedTo, movedTo.exists());
    }
  }

  @Test
  public void testAddRedirect() throws Exception {
    final String fromName = "health-monitor-troubleshooting";
    final String toName = "moved";
    final File fromFile = new File(docroot, fromName);
    final File toFile = new File(docroot, toName);
    mover.move(fromName, toName);

    verify(redirectHandler, times(1)).notifyRedirect(fromFile, toFile);
  }

  @Test
  public void testNotAddRedirect() throws Exception {
    mover.move("lasdjf;", "l;dkajf;");
    verify(redirectHandler, times(0)).notifyRedirect(any(), any());
  }

  @Test
  public void testReplay() throws Exception {
    String moved1Text = FileUtils.readFileToString(new File(docroot, "health-monitor-troubleshooting/index.md"), "UTF-8");
    mover.move("health-monitor-troubleshooting", "moved1");
    mover.move("overview-of-health-monitors", "moved2");
    File moved1 = new File(docroot, "moved1");
    File moved2 = new File(docroot, "moved2");
    assertTrue(moved1.isDirectory());
    assertTrue(moved2.isDirectory());
    FileUtils.moveDirectory(moved1, new File(docroot, "health-monitor-troubleshooting"));
    FileUtils.moveDirectory(moved2, new File(docroot, "overview-of-health-monitors"));
    assertFalse(moved1.isDirectory());
    assertFalse(moved2.isDirectory());
    mover.replay();
    assertTrue(moved1.isDirectory());
    assertTrue(moved2.isDirectory());
  }

//  @Test
//  public void testFileIntegrityAfterLinkUpdate() throws Exception {
//    String moved1Text = new MarkdownCleaner().clean(FileUtils.readFileToString(new File(docroot, "health-monitor-troubleshooting/index.md"), "UTF-8"));
//    mover.move("health-monitor-troubleshooting", "moved1");
//    assertTrue(new File(docroot, "moved1").isDirectory());
//    assertEquals(moved1Text, new MarkdownCleaner().clean(FileUtils.readFileToString(new File(docroot, "moved1/index.md"), "UTF-8")));
//
//    mover.move("overview-of-health-monitors", "moved2");
//    assertEquals(moved1Text, FileUtils.readFileToString(new File(docroot, "moved1/index.md"), "UTF-8"));
//  }

  private void assertFileIntegrity() throws Exception {
    Iterator<File> filesWithLinks = getFileIterator(new String[]{".md"});
    while (filesWithLinks.hasNext()) {
      final File file = filesWithLinks.next();
      final String text = FileUtils.readFileToString(file, "UTF-8");
      System.out.println("TEXT:\n" + text);
      assertFalse("Markdown file contains html tag: " + file + "\n" + text, text.contains("<html>"));
      assertFalse("Markdown file contains body tag: " + file + "\n" + text, text.contains("<body>"));
    }
  }

  private void assertLinkIntegrity() throws Exception {
    final Iterator<File> filesWithLinks = getFileIterator(new String[]{".md", ".html"});

    while (filesWithLinks.hasNext()) {
      final File file = filesWithLinks.next();
      Document doc = Jsoup.parse(file, "UTF-8");
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
            assertTrue("Target for link doesn't exist!\n  link   : " + link + "\n  target : " + target + "\n  in file: " + file, target.exists());
          } else {
            // this is a relative link
            final File target = new File(file.getParentFile(), link);
            try {
              assertTrue("Target for link (" + link + ") doesn't exist!\n  target : " + target + "\n  in file: " + file, target.exists());
            } catch (AssertionError e) {
              final StringBuilder msg = new StringBuilder(e.getMessage()).append("\n");
              final File targetParent = target.getParentFile();
              msg.append("Target parent: ").append(targetParent).append("\n");
              if (targetParent.isDirectory()) {
                msg.append("Target parent contains:\n");
                for (String name : targetParent.list()) {
                  msg.append("  ").append(name).append("\n");
                }
              } else {
                msg.append("Target parent is not a directory.\n");
                if (!targetParent.exists()) {
                  msg.append("Target parent does not exist.\n");
                }
              }
              throw new AssertionError(msg);
            }
          }
        }
      }
    }
  }

  private Iterator<File> getFileIterator(String[] extensions) {
    return FileUtils.iterateFiles(docroot, new IOFileFilter() {
      @Override
      public boolean accept(File file) {
        return accept(null, file.getName());
      }

      @Override
      public boolean accept(File dir, String name) {
        for (String extension : extensions) {
          if (name.endsWith(extension)) {
            return true;
          }
        }
        return false;
      }
    }, TrueFileFilter.INSTANCE);
  }

  private void assertFileInDirectory(final File dir, final String filename) {
    final String cleanFilename = filename.replaceAll("/", "");
    String msg = "File not found in directory:\n  dir : " + dir + "\n  file: " + filename + "\n";
    if (dir.isDirectory()) {
      msg += "  dir contents:\n";
      for (String name : dir.list()) {
        msg += "    " + name + "\n";
      }
    } else if (dir.isFile()) {
      msg += "  directory is actually a file";
    } else if (!dir.exists()) {
      msg += "  directory doesn't exist";
    }
    assertEquals(msg, 1, dir.list((dir1, name) -> cleanFilename.equals(name)).length);
  }

}