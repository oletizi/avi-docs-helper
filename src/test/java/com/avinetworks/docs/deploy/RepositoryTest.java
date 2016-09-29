package com.avinetworks.docs.deploy;

import com.avinetworks.docs.deploy.Repository;
import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RepositoryTest {

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private Repository repo;
  private File outdir;
  private ExecutorFactory executorFactory;
  private Executor executor;
  private CommandLine cmd;
  private String repoURL;

  @Before
  public void before() throws Exception {
    outdir = tmp.newFolder();
    executorFactory = mock(ExecutorFactory.class);
    executor = mock(Executor.class);

    when(executorFactory.newExecutor()).thenReturn(executor);

    final ArgumentCaptor<CommandLine> cliCaptor = ArgumentCaptor.forClass(CommandLine.class);

    when(executor.execute(cliCaptor.capture())).thenAnswer(invocationOnMock -> {
      cmd = cliCaptor.getValue();
      return 0;
    });
    repoURL = "https://github.com/oletizi/avi-docs.git";
    repo = new Repository(repoURL, outdir, executorFactory);
  }

  @Test
  public void testClone() throws Exception {
    assertEquals(0, outdir.list().length);
    repo.cloneOrPull();
    verify(executorFactory, times(1)).newExecutor();
    assertNotNull(cmd);
    assertEquals("git", cmd.getExecutable());
    assertArrayEquals(new String[] {"clone", repoURL}, cmd.getArguments());

    verify(executor, times(1)).setWorkingDirectory(outdir);
    verify(executor, times(1)).execute(cmd);
  }

  @Test
  public void testPull() throws Exception {
    final File repoDir = new File(outdir, "avi-docs");
    repoDir.mkdir();
    assertTrue(repoDir.isDirectory());

    repo.cloneOrPull();
    verify(executorFactory, times(1)).newExecutor();
    assertEquals("git", cmd.getExecutable());
    assertArrayEquals(new String[] {"pull"}, cmd.getArguments());

    verify(executor, times(1)).setWorkingDirectory(repoDir);
    verify(executor, times(1)).execute(cmd);
  }

  @Test
  @Ignore
  public void testCloneOrPullIT() throws Exception {
    final File repoDir = new File(outdir, "avi-docs");
    final Executor exec = new DefaultExecutor();
    final Repository repo = new Repository(repoURL, outdir, () -> exec);

    repo.cloneOrPull();
    assertEquals(outdir, exec.getWorkingDirectory());

    repo.cloneOrPull();
    assertEquals(repoDir, exec.getWorkingDirectory());
  }
}