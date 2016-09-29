package com.avinetworks.docs.web;

import com.avinetworks.docs.exec.ExecutorFactory;
import com.avinetworks.docs.repo.Repository;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
import spark.Request;
import spark.Response;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static spark.Spark.*;

public class PushHandler {
  private static final File OUTDIR = new File(System.getProperty("user.home"), ".avi-docs-repo");
  private static final String REPO_URL = "https://github.com/oletizi/avi-docs.git";
  private final Request req;
  private final Response res;
  private Repository repo;

  private PushHandler(final Request req, final Response res, final Repository repo) {
    this.req = req;
    this.res = res;
    this.repo = repo;
  }


  public static void main(String[] args) throws IOException {
    if (! OUTDIR.isDirectory()) {
      FileUtils.forceMkdir(OUTDIR);
      if (! OUTDIR.isDirectory()) {
        throw new RuntimeException("Can't create output directory: " + OUTDIR);
      }
    }
    final Repository repo = new Repository(REPO_URL, OUTDIR, DefaultExecutor::new);
    get("/helper/push", (req, res) -> new PushHandler(req, res, repo).doGet());
  }

  private Object doGet() throws IOException {
    repo.cloneOrPull();
    return "Hello from doGet!";
  }
}
