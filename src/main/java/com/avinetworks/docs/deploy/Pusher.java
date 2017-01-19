package com.avinetworks.docs.deploy;

import org.apache.commons.exec.Executor;

import java.io.File;

public class Pusher extends DeployAtom {

  public Pusher(final File repoDir, final File dest, final Executor executor) {
    super("bash", new String[] {"push.sh", dest.getAbsolutePath()}, new File(repoDir, "bin"), executor);
  }
}
