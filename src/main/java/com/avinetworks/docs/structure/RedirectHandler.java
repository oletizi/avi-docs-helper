package com.avinetworks.docs.structure;

import java.io.File;
import java.io.IOException;

/**
 * Created by orion on 9/13/16.
 */
public interface RedirectHandler {
  public void notifyRedirect(File oldLocation, File newLocation) throws IOException;
}
