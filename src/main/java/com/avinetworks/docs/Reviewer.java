package com.avinetworks.docs;

import java.io.*;

public class Reviewer {

  private final File sourceFile;
  private final File destFile;
  private final String hostA;
  private final String hostB;

  public Reviewer(File sourceFile, File destFile, String hostA, String hostB) {

    this.sourceFile = sourceFile;
    this.destFile = destFile;
    this.hostA = hostA;
    this.hostB = hostB;
  }

  public void run() throws IOException {
    final BufferedReader in = new BufferedReader(new FileReader(sourceFile));
    final PrintWriter out = new PrintWriter(new FileWriter(destFile));
    out.println("<html><head>");
    out.println("  <style>");
    out.println("    body { font-family: helvetica, sans-serif}");
    out.println("    li { margin-top: 10px }");
    out.println("  </style>");
    out.println("  </head><body><ol>");
    String line = null;
    while ((line = in.readLine()) != null) {
      if (! line.startsWith(" ")) {
        out.println("  <li>");
        out.println("    " + line + "<br/>");
        out.println("    " + makeLink(hostA, line) + "<br/>");
        out.println("    " + makeLink(hostB, line) + "<br/>");
        out.println("  </li>");
      }
    }
    out.println("</ol></body></html>");
    out.close();
  }

  private String makeLink(String host, String line) {
    String url = "http://" + host + "/" + line;
    return "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>";
  }

  public static void main(String[] args) throws IOException {
    final Reviewer reviewer = new Reviewer(new File(args[0]), new File(args[1]), "localhost:4000", "kbstage.avinetworks.com");
    reviewer.run();
  }
}
