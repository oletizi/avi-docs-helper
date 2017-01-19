package com.avinetworks.docs.deploy;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class RepositoryClone {

  private final String repoUrl;
  private final File cloneParentDir;
  private final String cloneName;
  private final String branchName;
  private final File cloneDir;

  RepositoryClone(final String repoUrl, File cloneParentDir, String cloneName, String branchName) {
    this.repoUrl = repoUrl;
    this.cloneParentDir = cloneParentDir;
    this.cloneName = cloneName;
    this.branchName = branchName;
    this.cloneDir = new File(cloneParentDir, cloneName);
  }

  public void cloneOrPull() throws IOException, InterruptedException, URISyntaxException {
    int status = -1;
    if (cloneDir.exists()) {
      status = snarfAndWaitFor(new ProcessBuilder("git", "pull").directory(cloneDir));
      if (status != 0) {
        throw new IOException("Unable to pull from " + repoUrl);
      }
    } else {
      // do a clone
      status = snarfAndWaitFor(new ProcessBuilder("git", "clone", repoUrl, cloneName).directory(cloneParentDir));
      if (status != 0) {
        throw new IOException("Unable to clone " + repoUrl + " to " + cloneDir);
      }
    }
    status = snarfAndWaitFor(new ProcessBuilder("git", "checkout", "-b", branchName).directory(cloneDir));
    if (status != 0) {
      throw new IOException("Unable to checkout " + branchName + " in clone: " + cloneDir);
    }
  }

  private int snarfAndWaitFor(final ProcessBuilder pb) throws IOException, InterruptedException {
    System.out.println("=======================");
    System.out.println("Working dir: " + pb.directory());
    System.out.println("Executing " + StringUtils.join(pb.command(), " "));
    final Process proc = pb.start();
    for (String line : IOUtils.readLines(proc.getInputStream(), "UTF-8")) {
      System.out.println(line);
    }
    for (String line : IOUtils.readLines(proc.getErrorStream(), "UTF-8")) {
      System.err.println(line);
    }
    return proc.waitFor();
  }
}
