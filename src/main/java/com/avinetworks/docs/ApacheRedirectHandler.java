package com.avinetworks.docs;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ApacheRedirectHandler implements RedirectHandler {

  static final String PREAMBLE = "# __MOVER_START__";
  static final String EPILOGUE = "# __MOVER_STOP__";

  private final File htaccess;
  private final File docroot;

  ApacheRedirectHandler(final File docroot) {
    this.docroot = docroot;
    htaccess = new File(docroot, ".htaccess");
  }

  @Override
  public void notifyRedirect(File oldLocation, File newLocation) throws IOException {
    final String oldPath = oldLocation.getCanonicalPath().replace(docroot.getCanonicalPath(), "");
    final String newPath = newLocation.getCanonicalPath().replace(docroot.getCanonicalPath(), "");

    final String redirect = "Redirect 301 " + oldPath + " " + newPath;
    final PrintWriter out;
    if (!htaccess.exists()) {
      out = openFile();
      out.println(PREAMBLE);
      out.println(redirect);
      out.println(EPILOGUE);
    } else {
      final String contents = FileUtils.readFileToString(htaccess, "UTF-8");
      out = openFile();
      for (String line : contents.split("\n")) {
        out.println(line);
        if (line.contains(PREAMBLE)) {
          out.println(redirect);
        }
      }
    }
    out.close();
  }

  private PrintWriter openFile() throws IOException {
    return new PrintWriter(new FileWriter(htaccess));
  }
}
