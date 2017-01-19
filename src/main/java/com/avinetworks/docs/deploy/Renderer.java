package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.DefaultExecutor;

import java.io.File;

public class Renderer extends DeployAtom {

  Renderer(File repoDir, ExecutorFactory execFactory) {
    super("bash", new String[]{"render.sh"}, new File(repoDir, "bin"), execFactory);
  }
}
