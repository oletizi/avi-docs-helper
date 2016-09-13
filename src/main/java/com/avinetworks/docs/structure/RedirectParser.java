package com.avinetworks.docs.structure;

import java.text.ParseException;

public interface RedirectParser {
  Redirect parse(String blob) throws ParseException;
}
