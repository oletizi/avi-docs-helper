package com.avinetworks.docs;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.codehaus.plexus.util.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Moves a page from one location to another while preserving link integrity
 */
public class Mover {

  //private static final Logger logger = LoggerFactory.getLogger(Mover.class);
  private final MoveLog log;
  private File docroot;

  public Mover(File docroot) throws IOException {
    this.docroot = docroot;
    log = new MoveLog(new File(docroot, ".move.log"));
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

    updateLinksTo(sourceFile, destFile);
    getMoveLog().logMove(source, dest);
  }

  private void updateLinksTo(final File sourceFile, final File destFile) throws IOException {
    final Iterator<File> markdownFiles = org.apache.commons.io.FileUtils.iterateFiles(docroot, new IOFileFilter() {
      @Override
      public boolean accept(File file) {
        return accept(null, file.getName());
      }

      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".md");
      }
    }, TrueFileFilter.INSTANCE);

    while (markdownFiles.hasNext()) {
      final File md = markdownFiles.next();
      final Document doc = Jsoup.parse(md, "UTF-8");
      boolean modified = false;
      for (final Element anchor : doc.select("a")) {
        if (anchor.hasAttr("href")) {
          // this is a link
          final String link = anchor.attr("href");
          String newLink = null;
          if (link.startsWith("http") || link.startsWith("ftp")) {
            // This is an external link. Nothing to do.
          } else if (link.startsWith("/")) {
            // This is an absolute link
            final File target = new File(docroot, link);
            if (sourceFile.getAbsolutePath().equals(target.getAbsolutePath())) {
              // the link matches old location; need to rewrite it to the new location
              newLink = destFile.getAbsolutePath().replace(docroot.getAbsolutePath(), "");
            }
          } else {
            // this is a relative link
            final File target = new File(md.getParentFile(), link);
            if (sourceFile.getAbsolutePath().equals(target.getAbsolutePath())) {
              newLink = destFile.getAbsolutePath().replace(md.getParentFile().getAbsolutePath(), "");
            }
          }
          if (newLink != null) {
            anchor.attr("href", newLink);
            modified = true;
          }
        }
      }
      if (modified) {
        FileUtils.fileWrite(md.getAbsolutePath(), doc.outerHtml());
      }
    }
  }

  public MoveLog getMoveLog() {
    return log;
  }
}
