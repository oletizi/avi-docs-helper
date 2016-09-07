package com.avinetworks.docs;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;

public class Server extends NanoHTTPD {

  private final String content;

  public Server(int port, String content) throws IOException {
    super(port);
    this.content = content;
    start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    System.out.println("\nServer running! Point your browers to http://localhost:" + port + "/ \n");
  }

  @Override
  public Response serve(final IHTTPSession session) {
    return newFixedLengthResponse(content);
  }

}
