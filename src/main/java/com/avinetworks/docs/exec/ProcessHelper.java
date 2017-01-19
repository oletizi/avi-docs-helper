package com.avinetworks.docs.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class ProcessHelper {

  private final ProcessBuilder pb;
  private final PrintStream out;
  private final PrintStream err;

  public ProcessHelper(final String... cmd) {
    this(System.out, System.err, cmd);
  }

  public ProcessHelper(final PrintStream out, final PrintStream err, String... cmd) {
    this.out = out;
    this.err = err;
    pb = new ProcessBuilder(cmd);
  }

  public ProcessHelper directory(final File dir) {
    pb.directory(dir);
    return this;
  }

  public int execute() throws IOException, InterruptedException {
    final String cmd = StringUtils.join(pb.command(), " ");

    out.println("=================");
    out.println("Working dir: " + pb.directory());
    out.println("Executing  : " + cmd);
    final Executor executor= new DefaultExecutor();
    executor.setWorkingDirectory(pb.directory());
    try {
      final int status =  executor.execute(CommandLine.parse(cmd));
      out.println("Finished   : " + cmd);
      return status;
    } catch (IOException e) {
      err.println("Exception caught while executing: " + cmd);
      e.printStackTrace();
      throw e;
    }
  }
}
