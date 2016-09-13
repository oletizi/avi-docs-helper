package com.avinetworks.docs.structure.apache;

import com.avinetworks.docs.structure.Redirect;
import com.avinetworks.docs.structure.RedirectParser;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

public class ApacheRedirectFactoryAndParserTest {

  @Test
  public void testParse() throws Exception {
    final RedirectParser unit = new ApacheRedirectFactoryAndParser();
    try {
      unit.parse("gobbledigook");
      fail("Should have thrown a parse exception");
    } catch (ParseException e) {
      // pass
    }

    // success case
    Redirect expected = new ApacheRedirect(301, "/foo", "bar");
    Redirect redirect = unit.parse("Redirect 301 /foo bar");
    assertNotNull(redirect);
    assertEquals(expected, redirect);
  }

}