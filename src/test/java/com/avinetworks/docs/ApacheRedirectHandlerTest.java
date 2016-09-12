package com.avinetworks.docs;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApacheRedirectHandlerTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File docroot;
  private ApacheRedirectHandler handler;
  private File htaccess;
  private File oldLocation;
  private File newLocation;
  private String preamble;
  private String epilogue;

  @Before
  public void before() throws Exception {
    docroot = tmp.newFolder();
    htaccess = new File(docroot, ".htaccess");
    handler = new ApacheRedirectHandler(docroot);
    oldLocation = new File(docroot, "old");
    newLocation = new File(docroot, "new");
    preamble = ApacheRedirectHandler.PREAMBLE;
    epilogue = ApacheRedirectHandler.EPILOGUE;
  }

  @Test
  public void testNotifyRedirect() throws Exception {
    assertFalse(htaccess.exists());
    handler.notifyRedirect(oldLocation, newLocation);
    assertTrue(htaccess.exists());
    assertContains("Redirect 301 /old /new");
  }

  @Test
  public void testAddRedirectToExistingFile() throws Exception {

    final PrintWriter out = new PrintWriter(new FileWriter(htaccess));
    out.println("# some stuff we don't care about");
    out.println(preamble);
    out.println("# some stuff we do care about");
    out.println(epilogue);
    out.println("# some more stuff we don't care about");
    out.close();

    assertContains(preamble);
    assertContains(epilogue);

    handler.notifyRedirect(oldLocation, newLocation);
    assertContains("# some stuff we don't care about");
    assertContains(preamble);
    assertContains("Redirect 301 /old /new");
    assertContains("# some stuff we do care about");
    assertContains(epilogue);
    assertContains("# some more stuff we don't care about");
  }

  private void assertContains(String text) throws Exception {
    assertTrue(contains(text));
  }

  private boolean contains(String text) throws Exception {
    return FileUtils.readFileToString(htaccess, "UTF-8").contains(text);
  }


}