package com.avinetworks.docs.structure.apache;

public class ApacheRedirect extends com.avinetworks.docs.structure.Redirect {
  public ApacheRedirect(int status, String oldPath, String newPath) {
    super(status, oldPath, newPath);
  }

  @Override
  public String toString() {
    return "Redirect " + getStatus() + " " + getSource() + " " + getTarget();
  }
}
