package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class Repository {
  public static final String REPO_URL = "https://github.com/oletizi/avi-docs.git";
  public static final File DEFAULT_REPO_DIR = new File(System.getProperty("user.home"), ".avi-docs-repo");
  public static final String DEFAULT_REPO_NAME = "avi-docs";

  static {
    try {
      FileUtils.forceMkdir(DEFAULT_REPO_DIR);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  private final String repoURL;
  private final File outdir;
  private final ExecutorFactory executorFactory;
  private final File repoDir;

  public Repository() throws MalformedURLException {
    this(REPO_URL, DEFAULT_REPO_DIR, DefaultExecutor::new);
  }

  public Repository(String repoURL, File outdir, ExecutorFactory executorFactory) throws MalformedURLException {
    this.repoURL = repoURL;
    this.outdir = outdir;
    this.executorFactory = executorFactory;
    final String repoName = repoURL.substring(repoURL.lastIndexOf('/') + 1, repoURL.lastIndexOf(".git"));
    repoDir = new File(outdir, repoName);
  }

  public void cloneOrPull() throws IOException {
    final CommandLine cmd = new CommandLine("git");
    final Executor exec = executorFactory.newExecutor();
    if (repoDir.isDirectory()) {
      // pull
      cmd.addArgument("pull");
      exec.setWorkingDirectory(repoDir);
    } else {
      // clone
      cmd.addArgument("clone");
      cmd.addArgument(repoURL);
      exec.setWorkingDirectory(outdir);
    }
    System.out.println("Executing: " + cmd);
    System.out.println("Working dir: " + exec.getWorkingDirectory());
    exec.execute(cmd);
  }
}
