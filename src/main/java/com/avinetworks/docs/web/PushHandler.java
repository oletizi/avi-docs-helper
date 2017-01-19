package com.avinetworks.docs.web;

import com.avinetworks.docs.deploy.Pusher;
import com.avinetworks.docs.deploy.RepositoryClone;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import static spark.Spark.*;

public class PushHandler {
  private static final Executor executor = Executors.newSingleThreadExecutor();
  private List<RepositoryClone> clones;

  private PushHandler(final List<RepositoryClone> clones) {
    this.clones = clones;
  }

  private Object doGet() throws IOException {
    for (final RepositoryClone clone : clones) {
      executor.execute(() -> {
        try {
          clone.cloneOrPull();
          //pusher.execute();
        } catch (IOException | InterruptedException | URISyntaxException e) {
          e.printStackTrace();
        }
      });
    }
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
    if (!configFile.isFile()) {
      System.out.println("Please specify a path to a valid configuration file: " + configFile);
      System.exit(-2);
    }

    final ObjectMapper mapper = new ObjectMapper();
    final PushHandlerConfig cfg = mapper.readValue(configFile, PushHandlerConfig.class);

    final List<RepositoryClone> clones = new ArrayList<>();

    for (PushHandlerConfig.Clone cloneCfg : cfg.getClones()) {
      clones.add(new RepositoryClone(cfg.getRepoUrl(), new File(cloneCfg.getParentDirectory()), cloneCfg.getCloneDirectory(), cloneCfg.getBranch()));
    }

    get("/helper/push", (req, res) -> new PushHandler(clones).doGet());
    post("/helper/push", (req, res) -> new PushHandler(clones).doPost());

  }
}
