package com.avinetworks.docs;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Moves a page from one location to another while preserving link integrity and adding a redirect entry in
 * .htaccess
 */
public class Mover {

  private File docroot;

  public Mover(File docroot) {

    this.docroot = docroot;
  }

  public void move(String source, String dest) throws IOException {
    final File sourceFile = new File(docroot, source);
    final File destFile = new File(docroot, dest);

    if (dest.endsWith("/")) {
      // we're moving the source into the directory dest
      FileUtils.forceMkdir(destFile);
      File destDir = new File(destFile, source);
      FileUtils.forceMkdir(destDir);
      FileUtils.copyDirectory(sourceFile, destDir);
      FileUtils.forceDelete(sourceFile);
    } else {
      // we're renaming the source to dest
      FileUtils.rename(sourceFile, destFile);
    }
  }
}
