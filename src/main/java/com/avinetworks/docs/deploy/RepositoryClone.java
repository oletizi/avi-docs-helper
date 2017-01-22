package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ProcessHelper;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;
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
  private final File pushDestination;
  private final File cloneDir;

  public RepositoryClone(final String repoUrl, final File cloneParentDir, final String cloneName, final String branchName,
                         final File pushDestination) {
    this.repoUrl = repoUrl;
    this.cloneParentDir = cloneParentDir;
    this.cloneName = cloneName;
    this.branchName = branchName;
    this.pushDestination = pushDestination;
    this.cloneDir = new File(cloneParentDir, cloneName);
  }

  public void cloneOrPull() throws IOException, InterruptedException, URISyntaxException {
    int status = -1;
    if (cloneDir.exists()) {
      status = new ProcessHelper("git pull").directory(cloneDir).execute();
      if (status != 0) {
        throw new IOException("Unable to pull from " + repoUrl);
      }
    } else {
      // do a clone
      FileUtils.forceMkdir(cloneParentDir);
      status = new ProcessHelper("git clone", repoUrl, cloneName).directory(cloneParentDir).execute();
      if (status != 0) {
        throw new IOException("Unable to clone " + repoUrl + " to " + cloneDir);
      }
    }

    new ProcessHelper("ls", "-l").directory(cloneParentDir).execute();

    status = new ProcessHelper("git checkout ", branchName).directory(cloneDir).execute();
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

  public File getCloneDirectory() {
    return cloneDir;
  }

  public File getPushDestination() {
    return pushDestination;
  }
}
