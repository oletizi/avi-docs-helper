package com.avinetworks.docs.web;

import java.util.ArrayList;
import java.util.List;

public class PushHandlerConfig {
  private String repoUrl;
  private List<Clone> clones;

  public PushHandlerConfig() {
    clones = new ArrayList<>();
  }

  public String getRepoUrl() {
    return repoUrl;
  }

  public void setRepoUrl(String repoUrl) {
    this.repoUrl = repoUrl;
  }

  public List<Clone> getClones() {
    return clones;
  }

  public void setClones(List<Clone> clones) {
    this.clones.clear();
    this.clones.addAll(clones);
  }

  public void addClone(Clone clone) {
    this.clones.add(clone);
  }

  public static final class Clone {
    private String branch;
    private String parentDirectory;
    private String cloneDirectory;

    public String getBranch() {
      return branch;
    }

    public void setBranch(String branch) {
      this.branch = branch;
    }

    public String getParentDirectory() {
      return parentDirectory;
    }

    public void setParentDirectory(String parentDirectory) {
      this.parentDirectory = parentDirectory;
    }

    public void setCloneDirectory(String cloneDirectory) {
      this.cloneDirectory = cloneDirectory;
    }

    public String getCloneDirectory() {
      return cloneDirectory;
    }
  }
}
