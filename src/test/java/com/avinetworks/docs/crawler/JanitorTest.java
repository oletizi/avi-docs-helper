package com.avinetworks.docs.crawler;

import com.avinetworks.docs.content.ContentConverter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class JanitorTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File docroot;
  private ContentConverter converter;
  private Janitor janitor;

  @Before
  public void before() throws Exception {
    docroot = tmp.newFolder();
    converter = mock(ContentConverter.class);
    janitor = new Janitor(docroot, "http://docs.avinetworks.com/", converter);
  }

  @Test
  @Ignore
  public void testBasics() throws Exception {
    janitor.cleanup();
  }

}