package com.avinetworks.docs.web;

import spark.Request;
import spark.Response;

import static spark.Spark.*;

public class PushHandler {
  private final Request req;
  private final Response res;

  private PushHandler(Request req, Response res) {
    this.req = req;
    this.res = res;
  }

  public static void main(String[] args) {
    get("/helper/push", (req, res) -> new PushHandler(req, res).doGet());
  }

  private Object doGet() {
    return "Hello from doGet!";
  }
}
