package com.avinetworks.docs.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

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

  @Test
  @Ignore
  public void createConfig() throws Exception {
    final PushHandlerConfig cfg = new PushHandlerConfig();
    cfg.setRepoUrl("git@github.com:oletizi/avi-docs.git");

    final PushHandlerConfig.Clone cloneMaster = new PushHandlerConfig.Clone();
    cloneMaster.setBranch("master");
    cloneMaster.setParentDirectory("/var/docs/");
    cloneMaster.setCloneName("avi-docs-master");
    cloneMaster.setPushDirectory(new File("/var/www/avi-docs-master/"));
    cfg.addClone(cloneMaster);

    final PushHandlerConfig.Clone clone17_1 = new PushHandlerConfig.Clone();
    clone17_1.setBranch("17.1");
    clone17_1.setParentDirectory("/var/docs/");
    clone17_1.setCloneName("avi-docs-17.1");
    clone17_1.setPushDirectory(new File("/var/www/avi-docs-17.1"));
    cfg.addClone(clone17_1);

    final String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cfg);
    System.out.println(out);

  }

}