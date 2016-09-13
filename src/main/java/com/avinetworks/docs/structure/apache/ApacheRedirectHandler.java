package com.avinetworks.docs.structure.apache;

import com.avinetworks.docs.structure.Redirect;
import com.avinetworks.docs.structure.RedirectFactory;
import com.avinetworks.docs.structure.RedirectHandler;
import com.avinetworks.docs.structure.RedirectParser;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.ParseException;
import java.util.Set;
import java.util.TreeSet;

public class ApacheRedirectHandler implements RedirectHandler {

  static final String PREAMBLE = "# __MOVER_START__";
  static final String EPILOGUE = "# __MOVER_STOP__";

  private final File htaccess;
  private final File docroot;
  private final Set<Redirect> redirects;
  private RedirectParser redirectParser;
  private RedirectFactory redirectFactory;

  public ApacheRedirectHandler(final File docroot, final RedirectFactory redirectFactory, final RedirectParser redirectParser) throws IOException {
    this.docroot = docroot;
    htaccess = new File(docroot, ".htaccess");
    this.redirectFactory = redirectFactory;
    this.redirectParser = redirectParser;
    redirects = new TreeSet<>();
    initHtaccess();
  }

  @Override
  public void notifyRedirect(File oldLocation, File newLocation) throws IOException {
    final String oldPath = oldLocation.getAbsolutePath().replace(docroot.getAbsolutePath(), "");
    final String newPath = newLocation.getAbsolutePath().replace(docroot.getAbsolutePath(), "");

    redirects.add(redirectFactory.create(301, oldPath, newPath));
    final PrintWriter out;
    final String contents = FileUtils.readFileToString(htaccess, "UTF-8");
    out = openFile();
    boolean writeRedirects = false;
    for (String line : contents.split("\n")) {
      if (line.contains(PREAMBLE)) {
        writeRedirects = true;
        out.println(line);
        for (Redirect redirect : redirects) {
          out.println(redirect);
        }
      } else if (line.contains(EPILOGUE)) {
        out.println(EPILOGUE);
        writeRedirects = false;
      } else if (writeRedirects) {
        // throw it away--it contains mover redirects
      } else {
        out.println(line);
      }
    }
    out.close();
  }

  private void initHtaccess() throws IOException {
    if (!htaccess.exists()) {
      PrintWriter out;
      out = openFile();
      out.println(PREAMBLE);
      out.println(EPILOGUE);
      out.close();
    } else {
      final BufferedReader in = new BufferedReader(new FileReader(htaccess));
      String line;
      boolean read = false;
      while ((line = in.readLine()) != null) {
        if (line.contains(PREAMBLE)) {
          read = true;
        } else if (line.contains(EPILOGUE)) {
          read = false;
        } else if (read) {
          try {
            redirects.add(redirectParser.parse(line));
          } catch (ParseException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  private PrintWriter openFile() throws IOException {
    return new PrintWriter(new FileWriter(htaccess));
  }

  public Set<Redirect> getRedirects() {
    return redirects;
  }
}
