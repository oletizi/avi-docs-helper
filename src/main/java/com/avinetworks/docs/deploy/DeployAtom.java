package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;

import java.io.File;
import java.io.IOException;

abstract class DeployAtom {
  private final String shell;
  private final String[] args;
  private final ExecutorFactory execFactory;
  private final File workingDir;

  DeployAtom(final String shell, final String[] args, final File workingDir, final ExecutorFactory execFactory) {
    this.shell = shell;
    this.args = args;
    this.workingDir = workingDir;
    this.execFactory = execFactory;
  }

  public void execute() throws IOException {
    final CommandLine cmd = new CommandLine(shell);
    for (String arg : args) {
      cmd.addArgument(arg);
    }

    final Executor executor = execFactory.newExecutor();
    executor.setWorkingDirectory(workingDir);
    System.out.println("Executing " + cmd);
    System.out.println("Working dir: " + executor.getWorkingDirectory());
    executor.execute(cmd);
  }
}
