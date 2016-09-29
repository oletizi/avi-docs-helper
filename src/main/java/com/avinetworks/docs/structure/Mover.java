package com.avinetworks.docs.structure;

import com.avinetworks.docs.content.MarkdownCleaner;
import com.avinetworks.docs.structure.apache.ApacheRedirectHandler;
import com.avinetworks.docs.structure.apache.ApacheRedirectFactoryAndParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Moves a page from one location to another while preserving link integrity & creating redirects
 */
public class Mover {

  //private static final Logger logger = LoggerFactory.getLogger(Mover.class);
  private final MoveLog log;
  private RedirectHandler redirectHandler;
  private File docroot;

  Mover(File docroot, RedirectHandler redirectHandler) throws IOException {
    this.docroot = docroot;
    log = new MoveLog(new File(docroot, ".move.log"));
    this.redirectHandler = redirectHandler;
  }

  File move(String source, String dest) throws IOException {
    return move(source, dest, true);
  }

  File move(String source, String dest, boolean doLog) throws IOException {
    final File sourceFile = new File(docroot, source);
    if (!sourceFile.exists()) {
      System.out.println("Source does not exist: " + sourceFile);
      return null;
    }
    final File destFile = new File(docroot, dest);
    File movedTo = null;
    if (sourceFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
      System.out.println("Source and destination are the same. Cowardly refusing to do anything.\n  source: "
          + sourceFile + "\n  dest  : " + destFile);
      return null;
    }
    if (destFile.isDirectory()) {
      // we're moving the source into the directory dest
      File destDir = new File(destFile, source);
      movedTo = destDir;
      //org.codehaus.plexus.util.FileUtils.forceMkdir(destDir);
      System.out.println("Moving:\n");
      System.out.println("  " + sourceFile);
      org.codehaus.plexus.util.FileUtils.copyDirectoryStructure(sourceFile, destDir);
      org.codehaus.plexus.util.FileUtils.forceDelete(sourceFile);
    } else {
      // we're renaming the source to dest
      org.codehaus.plexus.util.FileUtils.rename(sourceFile, destFile);
      movedTo = destFile;
    }

    updateLinksTo(sourceFile, destFile);
    redirectHandler.notifyRedirect(sourceFile, destFile);
    if (doLog) {
      getMoveLog().logMove(source, dest);
    }
    return movedTo;
  }

  private void updateLinksTo(final File sourceFile, final File destFile) throws IOException {
    final Iterator<File> filesWithLinks = FileUtils.iterateFiles(docroot, new IOFileFilter() {
      @Override
      public boolean accept(File file) {
        return accept(null, file.getName());
      }

      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".md") || name.endsWith(".html");
      }
    }, TrueFileFilter.INSTANCE);

    while (filesWithLinks.hasNext()) {
      final File htmlFile = filesWithLinks.next();
      final Document doc = Jsoup.parse(htmlFile, "UTF-8");
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
            final File target = new File(htmlFile.getParentFile(), link);
            if (sourceFile.getAbsolutePath().equals(target.getAbsolutePath())) {
              newLink = destFile.getAbsolutePath().replace(htmlFile.getParentFile().getAbsolutePath(), "");
            }
          }
          if (newLink != null) {
            anchor.attr("href", newLink);
            modified = true;
          }
        }
      }
      if (modified) {
        doc.outputSettings().prettyPrint(false);
        String updated = doc.select("body").html();
        updated = new MarkdownCleaner().clean(updated);
        org.codehaus.plexus.util.FileUtils.fileWrite(htmlFile.getAbsolutePath(), updated);
      }
    }
  }


  void replay() throws IOException {
    for (MoveLog.MoveLogEntry entry : getMoveLog().getEntries()) {
      move(entry.getSrc(), entry.getDest(), false);
    }
  }

  MoveLog getMoveLog() {
    return log;
  }

  public static void main(final String[] args) throws Exception {
    final File docroot = new File(System.getProperty("docroot"));
    final ApacheRedirectFactoryAndParser factoryAndParser = new ApacheRedirectFactoryAndParser();
    final Mover mover = new Mover(docroot, new ApacheRedirectHandler(docroot, factoryAndParser, factoryAndParser));
    if (args.length == 1 && args[0].equals("replay")) {
      mover.replay();
    } else if (args.length < 2) {
      System.out.print(usage());
    } else {
      // create a new Mover and perform the move
      final String src = args[0];
      final String dest = args[1];
      mover.move(src, dest, true);
    }
  }

  private static String usage() {
    String rv = "Mover -- moves files\n\n";
    rv += "Usage:\n\t Mover <source> <dest>\n\nNote: use only from the document root.\n\n";
    return rv;
  }
}
