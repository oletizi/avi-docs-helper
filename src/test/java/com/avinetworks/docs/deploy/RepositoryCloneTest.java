package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ProcessHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

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
  private File pushDirectory;

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

    int status = new ProcessHelper("git", "init").directory(repoDir).execute();
    assertEquals("Failed to init repoDir: " + repoDir, 0, status);

    status = new ProcessHelper("git", "add", ".").directory(repoDir).execute();
    assertEquals("Failed to add files to repoDir: " + repoDir, 0, status);

    status = new ProcessHelper("git", "commit", "-m", "initial commit").directory(repoDir).execute();
    assertEquals("Failed to commit to repoDir: " + repoDir, 0, status);

    status = new ProcessHelper("git", "branch", branchName).directory(repoDir).execute();
    assertEquals("Failed to create branch: " + branchName + " in repo: " + repoDir, 0, status);

    new ProcessHelper("git", "branch").directory(repoDir).execute();

//    status = execute(new ProcessBuilder("git", "add", ".").directory(repoDir));
//    assertEquals("Unable to add before commit", 0, status);
//
//    status = execute(new ProcessBuilder("git", "commit", "-m", "created a nice branch.").directory(repoDir));
//    assertEquals("Failed to check in the new branch: " + branchName + " in repo: " + repoDir, 0, status);
    clonedTestFile = new File(cloneDir, testFile.getName());
    pushDirectory = tmp.newFolder();
  }

  @Test
  @Ignore
  public void testClone() throws Exception {
    final RepositoryClone clone = new RepositoryClone(repoUrl, cloneDir.getParentFile(), "clone", branchName, pushDirectory);

    assertFalse(cloneDir.exists());
    clone.cloneOrPull();
    assertTrue(cloneDir.exists());
    assertTrue(clonedTestFile.isFile());

    assertEquals(FileUtils.readFileToString(testFile, "UTF-8"), FileUtils.readFileToString(clonedTestFile, "UTF-8"));
  }

  @Test
  @Ignore
  public void testPull() throws Exception {
    final RepositoryClone clone = new RepositoryClone(repoUrl, cloneDir.getParentFile(), "clone", "master", pushDirectory);

    assertFalse(cloneDir.exists());
    clone.cloneOrPull();
    assertTrue(cloneDir.exists());

    assertEquals(testContents, FileUtils.readFileToString(clonedTestFile, "UTF-8"));
    FileUtils.writeStringToFile(testFile, "changed", "UTF-8");

    int status = new ProcessHelper("git", "add", ".").directory(repoDir).execute();
    assertEquals(0, status);

    status = new ProcessHelper("git", "commit", "-m", "a nice change").directory(repoDir).execute();
    assertEquals(0, status);

    assertNotEquals(FileUtils.readFileToString(testFile, "UTF-8"), FileUtils.readFileToString(clonedTestFile, "UTF-8"));

    clone.cloneOrPull();

    assertEquals(FileUtils.readFileToString(testFile, "UTF-8"), FileUtils.readFileToString(clonedTestFile, "UTF-8"));
  }



}