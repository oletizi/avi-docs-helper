package com.avinetworks.docs.structure.apache;

import com.avinetworks.docs.structure.Redirect;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApacheRedirectTest {

  @Test
  public void testBasics() throws Exception {
    final Redirect unit = new ApacheRedirect(301, "foo", "bar");
    assertEquals(301, unit.getStatus());
    assertEquals("foo", unit.getSource());
    assertEquals("bar", unit.getTarget());
    Redirect compare = new ApacheRedirect(301, "foo", "bar");
    assertEquals(compare, unit);
    assertEquals(compare.hashCode(), unit.hashCode());

    assertEquals(0, compare.compareTo(unit));
    assertTrue(new ApacheRedirect(301, "aaaa", "bar").compareTo(unit) < 0);
    assertTrue(new ApacheRedirect(301, "ggg", "bar").compareTo(unit) > 0);

    assertEquals("Redirect 301 foo bar", unit.toString());
  }

}