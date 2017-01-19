package com.avinetworks.docs.deploy;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;

import java.io.File;
import java.io.IOException;

abstract class DeployAtom {
  private final String shell;
  private final String[] args;
  private final File workingDir;
  private final Executor executor;

  DeployAtom(final String shell, final String[] args, final File workingDir, final Executor executor) {
    this.shell = shell;
    this.args = args;
    this.workingDir = workingDir;
    this.executor = executor;
  }

  public void execute() throws IOException {
    final CommandLine cmd = new CommandLine(shell);
    for (String arg : args) {
      cmd.addArgument(arg);
    }
    executor.setWorkingDirectory(workingDir);
    System.out.println("Executing " + cmd);
    System.out.println("Working dir: " + executor.getWorkingDirectory());
    executor.execute(cmd);
  }
}
