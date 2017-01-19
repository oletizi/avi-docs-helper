package com.avinetworks.docs.web;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PushHandlerTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File configFile;
  private File branchA;
  private File branchB;
  private String repoUrl;
  private File branchADest;
  private File branchBDest;

  @Before
  public void before() throws Exception {

    final File folder = tmp.newFolder();
    final File repoDir = new File(folder, "repo");
    FileUtils.forceMkdir(repoDir);
    final File testFile = new File(repoDir, "test.txt");
    FileUtils.writeStringToFile(testFile, "Hello, world.", "UTF-8");

    // init the git repo
    int status = new ProcessBuilder("git", "init").directory(repoDir).start().waitFor();
    assertEquals("Git init did not return normally.", 0, status);
    // add all files to the repo
    status = new ProcessBuilder("git", "add", ".").directory(repoDir).start().waitFor();
    assertEquals("Git add failed.", 0, status);
    // commit
    status = new ProcessBuilder("git", "commit", "-m", "a nice new file").directory(repoDir).start().waitFor();
    assertEquals("Git commit failed", 0, status);

    repoUrl = "file://" + repoDir.getCanonicalPath();

    branchA = new File(folder, "branchA");
    branchADest = new File(folder, "branchADest");

    branchB = new File(folder, "branchB");
    branchBDest = new File(folder, "branchBDest");

    configFile = new File(folder, "push-config.yml");
    final PrintWriter out = new PrintWriter(new FileWriter(configFile));
    out.println("--- sample push config file");
    out.println("repo-url: " + repoUrl);
    out.println("clones:");
    out.println("  - parentDirectory: " + branchA + ":");
    out.println("    branch: branchA");
    out.println("    destination-directory: " + branchADest);
    out.println("  - parentDirectory: " + branchB + ":");
    out.println("    branch: branchB");
    out.println("    destination-directory: " + branchBDest);
    out.close();
  }

  @Test
  @Ignore
  public void testMain() throws Exception {
    assertFalse(branchA.exists());
    assertFalse(branchADest.exists());
    assertFalse(branchB.exists());
    assertFalse(branchBDest.exists());

    final Thread runner = new Thread(() -> {
      try {
        PushHandler.main(new String[]{configFile.getCanonicalPath()});
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    runner.start();
    Thread.sleep(2000);
    final HttpGet get = new HttpGet("http://localhost:4567/helper/push");
    final CloseableHttpClient client = HttpClients.createDefault();
    final CloseableHttpResponse response = client.execute(get);
    assertEquals(response.getStatusLine().getReasonPhrase(), 200, response.getStatusLine().getStatusCode());

    assertTrue(branchA.isDirectory());
    assertTrue(branchADest.isDirectory());
    assertTrue(branchB.isDirectory());
    assertTrue(branchBDest.isDirectory());
  }

}