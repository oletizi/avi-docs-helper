package com.avinetworks.docs.web;

import java.io.File;
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
    private String cloneName;
    private String pushDirectory;

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

    /**
     * Sets name of the directory the repository will be cloned into inside the parent directory
     * @param cloneName
     */
    public void setCloneName(String cloneName) {
      this.cloneName = cloneName;
    }

    public String getCloneName() {
      return cloneName;
    }

    public File getPushDirectory() {
      return new File(pushDirectory);
    }

    public void setPushDirectory(File pushDirectory) {
      this.pushDirectory = pushDirectory.getAbsolutePath();
    }
  }
}
