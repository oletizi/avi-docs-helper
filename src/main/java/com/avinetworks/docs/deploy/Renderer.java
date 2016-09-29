package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.DefaultExecutor;

import java.io.File;

public class Renderer extends DeployAtom {

  public Renderer() {
    this(new File(Repository.DEFAULT_REPO_DIR, Repository.DEFAULT_REPO_NAME), DefaultExecutor::new);
  }

  Renderer(File repoDir, ExecutorFactory execFactory) {
    super("bash", "render.sh", new File(repoDir, "bin"), execFactory);
  }
}
