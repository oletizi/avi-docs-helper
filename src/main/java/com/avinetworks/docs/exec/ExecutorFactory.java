package com.avinetworks.docs.exec;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.Executor;

public interface ExecutorFactory {
  Executor newExecutor();
}
