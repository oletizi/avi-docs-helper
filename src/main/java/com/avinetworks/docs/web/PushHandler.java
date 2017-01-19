package com.avinetworks.docs.web;

import com.avinetworks.docs.deploy.Pusher;
import com.avinetworks.docs.deploy.RepositoryClone;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PushHandler {
  private static final Executor executor = Executors.newSingleThreadExecutor();
  private RepositoryClone repo;
  private Pusher pusher;

  private PushHandler(final RepositoryClone repo, Pusher pusher) {
    this.repo = repo;
    this.pusher = pusher;
  }

  private Object doGet() throws IOException {
    executor.execute(() -> {
      try {
        repo.cloneOrPull();
        pusher.execute();
      } catch (IOException | InterruptedException | URISyntaxException e) {
        e.printStackTrace();
      }
    });
    return "Success!";
  }

  private Object doPost() throws IOException {
    return doGet();
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.out.println("Please specify a configuration path");
      System.exit(-1);
    }
    final File configFile = new File(args[0]);
    if (! configFile.isFile()) {
      System.out.println("Please specify a path to a valid configuration file: " + configFile);
      System.exit(-2);
    }

    ObjectMapper mapper = new ObjectMapper();

    //final String repoUrl = (String) yaml.get("repo-url");
    /*
    final RepositoryClone repo = new RepositoryClone();
    //final Renderer renderer = new Renderer();
    final Pusher pusher = new Pusher();
    get("/helper/push", (req, res) -> new PushHandler(repo, pusher).doGet());
    post("/helper/push", (req, res) -> new PushHandler(repo, pusher).doPost());
    */
  }
}
