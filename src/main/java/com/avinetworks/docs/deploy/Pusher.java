package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.DefaultExecutor;

import java.io.File;

public class Pusher extends DeployAtom {

  public Pusher() {
    this(new File(Repository.DEFAULT_REPO_DIR, Repository.DEFAULT_REPO_NAME), DefaultExecutor::new);
  }

  public Pusher(final File repoDir, final ExecutorFactory execFactory) {
    super("bash", "push.sh", new File(repoDir, "bin"), execFactory);
  }

}
