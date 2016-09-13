package com.avinetworks.docs.structure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by orion on 9/13/16.
 */
class MoveLog {
  private final File file;
  private final List<MoveLogEntry> entries;
  private final ObjectMapper mapper;

  MoveLog(final File file) throws IOException {
    this.file = file;
    mapper = new ObjectMapper();
    if (file.exists()) {
      // load entries from file
      entries = mapper.readValue(file, List.class);
    } else {
      entries = new ArrayList<>();
    }
  }

  void logMove(final String src, final String dest) throws IOException {
    entries.add(new MoveLogEntry(src, dest, new DateTime()));
    mapper.writerWithDefaultPrettyPrinter().writeValue(file, entries);
  }

  List<MoveLogEntry> getEntries() {
    return new ArrayList<>(entries);
  }

  static class MoveLogEntry {
    @JsonProperty
    private final String src;
    @JsonProperty
    private final String dest;
    @JsonProperty
    private final DateTime timestamp;

    MoveLogEntry(final String src, final String dest, final DateTime timestamp) {
      this.src = src;
      this.dest = dest;
      this.timestamp = timestamp;
    }

    public String getSrc() {
      return src;
    }

    public String getDest() {
      return dest;
    }

    public DateTime getTimestamp() {
      return timestamp;
    }
  }

}
