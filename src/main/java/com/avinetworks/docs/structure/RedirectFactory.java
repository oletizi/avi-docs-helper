package com.avinetworks.docs.structure;

public interface RedirectFactory {
  Redirect create(int i, String oldPath, String newPath);
}
