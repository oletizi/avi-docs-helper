package com.avinetworks.docs.deploy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class RepositoryCloneTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File repoDir;
  private String repoUrl;
  private File cloneDir;
  private File testFile;
  private String branchName;
  private String testContents;
  private File clonedTestFile;

  @Before
  public void before() throws Exception {
    branchName = "myBranch";
    final File parentFolder = tmp.newFolder();
    repoDir = new File(parentFolder, "repoDir");
    cloneDir = new File(parentFolder, "clone");
    repoUrl = "file://" + repoDir.getCanonicalPath();

    FileUtils.forceMkdir(repoDir);
    testFile = new File(repoDir, "test.txt");
    testContents = "Hello, world!";
    FileUtils.writeStringToFile(testFile, testContents, "UTF-8");

    int status = snarfAndWaitFor(new ProcessBuilder("git", "init").directory(repoDir));
    assertEquals("Failed to init repoDir: " + repoDir, 0, status);

    status = snarfAndWaitFor(new ProcessBuilder("git", "add", ".").directory(repoDir));
    assertEquals("Failed to add files to repoDir: " + repoDir, 0, status);

    status = snarfAndWaitFor(new ProcessBuilder("git", "commit", "-m", "initial commit").directory(repoDir));
    assertEquals("Failed to commit to repoDir: " + repoDir, 0, status);

    status = snarfAndWaitFor(new ProcessBuilder("git", "branch", branchName).directory(repoDir));
    assertEquals("Failed to create branch: " + branchName + " in repo: " + repoDir, 0, status);

//    status = snarfAndWaitFor(new ProcessBuilder("git", "add", ".").directory(repoDir));
//    assertEquals("Unable to add before commit", 0, status);
//
//    status = snarfAndWaitFor(new ProcessBuilder("git", "commit", "-m", "created a nice branch.").directory(repoDir));
//    assertEquals("Failed to check in the new branch: " + branchName + " in repo: " + repoDir, 0, status);
    clonedTestFile = new File(cloneDir, testFile.getName());
  }

  @Test
  public void testClone() throws Exception {
    final RepositoryClone clone = new RepositoryClone(repoUrl, cloneDir.getParentFile(), "clone", branchName);

    assertFalse(cloneDir.exists());
    clone.cloneOrPull();
    assertTrue(cloneDir.exists());
    assertTrue(clonedTestFile.isFile());

    assertEquals(FileUtils.readFileToString(testFile, "UTF-8"), FileUtils.readFileToString(clonedTestFile, "UTF-8"));
  }

  @Test
  public void testPull() throws Exception {
    final RepositoryClone clone = new RepositoryClone(repoUrl, cloneDir.getParentFile(), "clone", "master");

    assertFalse(cloneDir.exists());
    clone.cloneOrPull();
    assertTrue(cloneDir.exists());

    assertEquals(testContents, FileUtils.readFileToString(clonedTestFile, "UTF-8"));
    FileUtils.writeStringToFile(testFile, "changed", "UTF-8");

    int status = snarfAndWaitFor(new ProcessBuilder("git", "add", ".").directory(repoDir));
    assertEquals(0, status);

    status = snarfAndWaitFor(new ProcessBuilder("git", "commit", "-m", "a nice change").directory(repoDir));
    assertEquals(0, status);

    assertNotEquals(FileUtils.readFileToString(testFile, "UTF-8"), FileUtils.readFileToString(clonedTestFile, "UTF-8"));

    clone.cloneOrPull();

    assertEquals(FileUtils.readFileToString(testFile, "UTF-8"), FileUtils.readFileToString(clonedTestFile, "UTF-8"));
  }

  private int snarfAndWaitFor(final ProcessBuilder pb) throws Exception {

    System.out.println("=======================");
    System.out.println("Working dir: " + pb.directory());
    System.out.println("Executing " + StringUtils.join(pb.command(), " "));
    final Process proc = pb.start();
    for (String line : IOUtils.readLines(proc.getInputStream(), "UTF-8")) {
      System.out.println(line);
    }
    for (String line : IOUtils.readLines(proc.getErrorStream(), "UTF-8")) {
      System.err.println(line);
    }
    return proc.waitFor();
  }

}