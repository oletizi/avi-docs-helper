package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.DefaultExecutor;

import java.io.File;

public class Pusher extends DeployAtom {

  private Pusher(final File repoDir, final ExecutorFactory execFactory) {
    super("bash", new String[] {"push.sh", "local"}, new File(repoDir, "bin"), execFactory);
  }

}
