package com.avinetworks.docs.web;

import com.avinetworks.docs.exec.ProcessHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PushHandlerTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File configFile;
  private File branchA;
  private File branchB;
  private File branchADest;
  private File branchBDest;
  private File repoDir;

  @Before
  public void before() throws Exception {

    final File folder = tmp.newFolder();
    repoDir = new File(folder, "repo");
    assertFalse(repoDir.exists());
    final String repoUrl = repoDir.getAbsolutePath();

    branchA = new File(folder, "branchA");
    branchADest = new File(folder, "branchADest");

    branchB = new File(folder, "branchB");
    branchBDest = new File(folder, "branchBDest");

    configFile = new File(folder, "push-config.json");

    FileUtils.forceMkdir(repoDir);
    final File testFile = new File(repoDir, "test.txt");
    FileUtils.writeStringToFile(testFile, "Hello, world.", "UTF-8");

    // init the git repo
    int status = new ProcessHelper("git", "init").directory(repoDir).execute();
    assertEquals("Git init did not return normally.", 0, status);
    // add all files to the repo
    status = new ProcessHelper("git", "add", ".").directory(repoDir).execute();
    assertEquals("Git add failed.", 0, status);
    // commit
    status = new ProcessHelper("git", "commit", "-m", "'a nice new file'").directory(repoDir).execute();
    assertEquals("Git commit failed", 0, status);
    // create the branches
    status = new ProcessHelper("git", "branch", branchA.getName()).directory(repoDir).execute();
    assertEquals("Git create branch failed: " + branchA.getName(), 0, status);

    new ProcessHelper("git", "branch").directory(repoDir).execute();

    final PushHandlerConfig cfg = new PushHandlerConfig();
    PushHandlerConfig.Repository repo = new PushHandlerConfig.Repository();
    cfg.getRepos().add(repo);
    repo.setRepoUrl(repoUrl);
    PushHandlerConfig.Clone clone = new PushHandlerConfig.Clone();
    clone.setBranch(branchA.getName());
    clone.setParentDirectory(branchA.getParent());
    clone.setCloneName(branchA.getName());
    clone.setPushDirectory(branchADest);

    repo.getClones().add(clone);
    mapper.writeValue(configFile, cfg);
  }

  @Test
  @Ignore
  public void testMain() throws Exception {
    assertTrue(configFile.isFile());
    assertTrue("repo does not exist: " + repoDir, repoDir.isDirectory());
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

    System.out.println("Waiting for stuff to happen...");
    Thread.sleep(10 * 1000);

    assertTrue("branchA didn't get cloned", branchA.isDirectory());
    assertTrue("The push for branchA didn't happen", branchADest.isDirectory());
    assertTrue("branchB didn't get cloned", branchB.isDirectory());
    assertTrue("The push for branchB didn't happen", branchBDest.isDirectory());
  }

}