package com.avinetworks.docs;

import java.io.File;
import java.io.IOException;

public interface RedirectHandler {
  public void notifyRedirect(File oldLocation, File newLocation) throws IOException;
}
