package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;

import java.io.File;
import java.io.IOException;

abstract class DeployAtom {
  private final String shell;
  private final String command;
  private final ExecutorFactory execFactory;
  private final File workingDir;

  DeployAtom(final String shell, final String command, final File workingDir, final ExecutorFactory execFactory) {
    this.shell = shell;
    this.command = command;
    this.workingDir = workingDir;
    this.execFactory = execFactory;
  }

  public void execute() throws IOException {
    final CommandLine cmd = new CommandLine(shell);
    cmd.addArgument(command);
    final Executor executor = execFactory.newExecutor();
    executor.setWorkingDirectory(workingDir);
    System.out.println("Executing " + cmd);
    System.out.println("Working dir: " + executor.getWorkingDirectory());
    executor.execute(cmd);
  }
}
