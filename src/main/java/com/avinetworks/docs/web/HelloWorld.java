package com.avinetworks.docs.web;

import static spark.Spark.*;

public class HelloWorld {
  public static void main(String[] args) {
    get("/hello", (req, res) -> "Hello World");
  }
}