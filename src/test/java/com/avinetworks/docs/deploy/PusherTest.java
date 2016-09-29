package com.avinetworks.docs.deploy;

import org.junit.Ignore;
import org.junit.Test;

public class PusherTest {

  @Test
  @Ignore
  public void pusherTestIT() throws Exception {
    new Repository().cloneOrPull();
    new Renderer().execute();
    new Pusher().execute();
  }

}