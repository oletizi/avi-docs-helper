package com.avinetworks.docs.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.*;

public class PushHandlerConfigTest {

  private ObjectMapper mapper;

  @Before
  public void before() throws Exception {
    mapper = new ObjectMapper();
  }

  @Test
  public void testBasics() throws Exception {
    final InputStream stream = getClass().getClassLoader().getResourceAsStream("config/config.json");
    assertNotNull(stream);
    final PushHandlerConfig config = mapper.readValue(stream, PushHandlerConfig.class);
  }

}