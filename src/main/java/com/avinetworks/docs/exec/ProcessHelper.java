package com.avinetworks.docs.exec;

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

  public int snarfAndWaitFor() throws IOException, InterruptedException {
    final Process proc = pb.start();
    for (String line : IOUtils.readLines(proc.getInputStream())) {
      out.println(line);
    }
    for (String line : IOUtils.readLines(proc.getErrorStream())) {
      err.println(line);
    }
    return proc.waitFor();
  }
}
