package com.avinetworks.docs.structure.apache;

import com.avinetworks.docs.structure.Redirect;
import com.avinetworks.docs.structure.RedirectFactory;
import com.avinetworks.docs.structure.RedirectParser;

import java.text.ParseException;

public class ApacheRedirectFactoryAndParser implements RedirectFactory, RedirectParser {
  public Redirect parse(final String line) throws ParseException {
    if (!line.startsWith("Redirect")) {
      throw new ParseException("Unknown redirect format: " + line, -1);

    }
    String[] split = line.split("\\s+");
    if (split.length != 4) {
      System.out.println("split length: " + split.length);
      throw new ParseException("Unknown redirect format: " + line, -1);
    }
    return new ApacheRedirect(Integer.parseInt(split[1]), split[2], split[3]);
  }

  @Override
  public Redirect create(int status, String oldPath, String newPath) {
    return new ApacheRedirect(status, oldPath, newPath);
  }
}
