package com.avinetworks.docs.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PushHandlerConfigTest {

  private ObjectMapper mapper;

  @Before
  public void before() throws Exception {
    mapper = new ObjectMapper();
  }

  @Test
  public void testBasics() throws Exception {
    final PushHandlerConfig cfg = new PushHandlerConfig();
    cfg.setRepoUrl("http://google.com/");
    PushHandlerConfig.Clone clone = new PushHandlerConfig.Clone();
    clone.setBranch("branch");
    clone.setParentDirectory("parentDir");
    clone.setCloneName("cloneDir");

    cfg.addClone(clone);

    clone = new PushHandlerConfig.Clone();
    clone.setBranch("branchB");
    clone.setParentDirectory("clone2parentDir");
    clone.setCloneName("clone2Dir");

    cfg.addClone(clone);

    final String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cfg);
    System.out.println("Config output: " + out);

    final PushHandlerConfig readConfig = mapper.readValue(out, PushHandlerConfig.class);
    assertEquals(cfg.getRepoUrl(), readConfig.getRepoUrl());
    assertEquals(cfg.getClones().size(), readConfig.getClones().size());
    assertEquals(out, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(readConfig));
  }

}