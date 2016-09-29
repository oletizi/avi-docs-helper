package com.avinetworks.docs.web;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class PushHandlerTest {

  @Test
  @Ignore
  public void testMain() throws Exception {
    final Thread runner = new Thread(() -> {
      try {
        PushHandler.main(new String[0]);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    runner.start();
    Thread.sleep(2000);
    final HttpGet get = new HttpGet("http://localhost:4567/helper/push");
    final CloseableHttpClient client = HttpClients.createDefault();
    client.execute(get);
  }

}