package com.avinetworks.docs.structure.apache;

import com.avinetworks.docs.structure.Redirect;
import com.avinetworks.docs.structure.RedirectFactory;
import com.avinetworks.docs.structure.RedirectHandler;
import com.avinetworks.docs.structure.RedirectParser;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ApacheRedirectHandlerTest {

  private static final String REDIRECT_A_STRING = "Redirect A";
  private static final String REDIRECT_B_STRING = "Redirect B";

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private ApacheRedirectHandler handler;
  private File htaccess;
  private File oldLocation;
  private File newLocation;
  private String preamble;
  private String epilogue;
  private RedirectParser redirectParser;
  private RedirectFactory redirectFactory;
  private File docroot;

  @Before
  public void before() throws Exception {
    docroot = tmp.newFolder();
    htaccess = new File(docroot, ".htaccess");
    redirectFactory = mock(RedirectFactory.class);
    redirectParser = mock(RedirectParser.class);
    handler = new ApacheRedirectHandler(docroot, redirectFactory, redirectParser);
    oldLocation = new File(docroot, "old");
    newLocation = new File(docroot, "new");
    preamble = ApacheRedirectHandler.PREAMBLE;
    epilogue = ApacheRedirectHandler.EPILOGUE;
  }

  @Test
  public void testNotifyRedirect() throws Exception {
    final String redirectString = "I'm the redirect";
    final Redirect redirect = mock(Redirect.class);
    when(redirect.toString()).thenReturn(redirectString);
    when(redirectFactory.create(301, extractPath(oldLocation), extractPath(newLocation))).thenReturn(redirect);
    handler.notifyRedirect(oldLocation, newLocation);
    System.out.println("HTACCESS\n" + FileUtils.readFileToString(htaccess, "UTF-8"));
    assertTrue(htaccess.exists());
    assertContains(redirectString);
  }

  @Test
  public void testAddRedirectToExistingFile() throws Exception {

    createHtaccess(REDIRECT_A_STRING + "\n" + REDIRECT_B_STRING);

    assertContains(preamble);
    assertContains(REDIRECT_A_STRING);
    assertContains(REDIRECT_B_STRING);
    assertContains(epilogue);

    Redirect redirectA = mock(Redirect.class);
    Redirect redirectB = mock(Redirect.class);
    when(redirectA.toString()).thenReturn(REDIRECT_A_STRING);
    when(redirectB.toString()).thenReturn(REDIRECT_B_STRING);

    when(redirectParser.parse(REDIRECT_A_STRING)).thenReturn(redirectA);
    when(redirectParser.parse(REDIRECT_B_STRING)).thenReturn(redirectB);

    Redirect newRedirect = mock(Redirect.class);
    when(newRedirect.toString()).thenReturn("I'm the redirect!");
    when(redirectFactory.create(301, extractPath(oldLocation), extractPath(newLocation))).thenReturn(newRedirect);
    handler = new ApacheRedirectHandler(docroot, redirectFactory, redirectParser);
    handler.notifyRedirect(oldLocation, newLocation);
    assertContains("# some stuff we don't care about");
    assertContains(preamble);
    assertContains(newRedirect.toString());
    assertContains(REDIRECT_A_STRING);
    assertContains(REDIRECT_B_STRING);
    assertContains(epilogue);
    assertContains("# some more stuff we don't care about");
  }

  private void createHtaccess(final String contents) throws IOException {
    final PrintWriter out = new PrintWriter(new FileWriter(htaccess));
    out.println("# some stuff we don't care about");
    out.println(preamble);
    out.println(contents);
    out.println(epilogue);
    out.println("# some more stuff we don't care about");
    out.close();
  }

  @Test
  public void testGetMoverRedirects() throws Exception {
    Redirect redirect = mock(Redirect.class);
    when(redirectParser.parse(any())).thenReturn(redirect);
    when(redirectFactory.create(301, extractPath(oldLocation), extractPath(newLocation))).thenReturn(redirect);
    assertEquals(0, handler.getRedirects().size());
    handler.notifyRedirect(oldLocation, newLocation);
    assertEquals(1, handler.getRedirects().size());
    assertEquals(redirect, new ArrayList<>(handler.getRedirects()).get(0));
  }

  private String extractPath(final File file) {
    return file.getAbsolutePath().replace(docroot.getAbsolutePath(), "");
  }

  private void assertContains(String text) throws Exception {
    assertTrue(contains(text));
  }

  private boolean contains(String text) throws Exception {
    return FileUtils.readFileToString(htaccess, "UTF-8").contains(text);
  }


}