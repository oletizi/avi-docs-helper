package com.avinetworks.docs.content;

import java.io.File;
import java.io.IOException;

public interface ContentConverter {
  void convert(String s, String content, File outDir) throws IOException;
}
