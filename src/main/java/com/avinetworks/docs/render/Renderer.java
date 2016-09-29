package com.avinetworks.docs.render;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;

import java.io.File;
import java.io.IOException;

public class Renderer {
  public static final String SHELL = "bash";
  public static final String RENDER_COMMAND = "render.sh";
  private final ExecutorFactory execFactory;
  private final File binDir;

  public Renderer(File repoDir, ExecutorFactory execFactory) {
    this.execFactory = execFactory;
    binDir = new File(repoDir, "bin");
  }

  public void render() throws IOException {
    final CommandLine cmd = new CommandLine(SHELL);
    cmd.addArgument(RENDER_COMMAND);
    final Executor executor = execFactory.newExecutor();
    executor.setWorkingDirectory(binDir);
    executor.execute(cmd);
  }
}
