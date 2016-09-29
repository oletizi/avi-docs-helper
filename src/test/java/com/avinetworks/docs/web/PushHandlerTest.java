package com.avinetworks.docs.web;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class PushHandlerTest {

  @Test
  public void testMain() throws Exception {
    final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(1);
    final Thread runner = new Thread(() -> {
      try {
        PushHandler.main(new String[0]);
      } catch (IOException e) {
        queue.add(e);
      }
    });
    runner.start();
    Thread.sleep(2000);
    final HttpGet get = new HttpGet("http://localhost:4567/helper/push");
    final CloseableHttpClient client = HttpClients.createDefault();
    CloseableHttpResponse response = client.execute(get);
  }

}