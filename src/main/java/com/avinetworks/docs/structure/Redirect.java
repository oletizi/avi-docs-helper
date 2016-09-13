package com.avinetworks.docs.structure;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Redirect implements Comparable<Redirect> {

  private final int status;
  private final String oldPath;
  private final String newPath;

  public Redirect(int status, String oldPath, String newPath) {

    this.status = status;
    this.oldPath = oldPath;
    this.newPath = newPath;
  }

  @Override
  public int compareTo(Redirect o) {
    return CompareToBuilder.reflectionCompare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    return EqualsBuilder.reflectionEquals(this, o);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  public int getStatus() {
    return status;
  }

  public String getSource() {
    return oldPath;
  }

  public String getTarget() {
    return newPath;
  }

  @Override
  public abstract String toString();
}
