package com.avinetworks.docs.repo;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

public class Repository {
  private final String repoURL;
  private final File outdir;
  private final ExecutorFactory executorFactory;
  private final String repoName;
  private final File repoDir;

  public Repository(String repoURL, File outdir, ExecutorFactory executorFactory) throws MalformedURLException {
    this.repoURL = repoURL;
    this.outdir = outdir;
    this.executorFactory = executorFactory;
    repoName = repoURL.substring(repoURL.lastIndexOf('/') + 1, repoURL.lastIndexOf(".git"));
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
    int status = exec.execute(cmd);
  }
}
