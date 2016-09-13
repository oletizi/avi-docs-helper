package com.avinetworks.docs.structure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class MoveLog {
  private final File file;
  @JsonDeserialize(contentAs = MoveLogEntry.class)
  private final List<MoveLogEntry> entries;
  private final ObjectMapper mapper;

  MoveLog(final File file) throws IOException {
    this.file = file;
    mapper = new ObjectMapper();
    if (file.exists()) {
      // load entries from file
      entries = mapper.readValue(file, new TypeReference<List<MoveLogEntry>>() {
      });
    } else {
      entries = new ArrayList<>();
    }
  }

  void logMove(final String src, final String dest) throws IOException {
    entries.add(new MoveLogEntry(src, dest, new Date()));
    mapper.writerWithDefaultPrettyPrinter().writeValue(file, entries);
  }

  List<MoveLogEntry> getEntries() {
    return new ArrayList<>(entries);
  }

  static class MoveLogEntry {
    @JsonProperty
    private String src;
    @JsonProperty
    private String dest;
    @JsonProperty
    private Date timestamp;

    MoveLogEntry() {
      // for Jackson
    }

    MoveLogEntry(final String src, final String dest, final Date timestamp) {
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

    public Date getTimestamp() {
      return timestamp;
    }
  }

}
