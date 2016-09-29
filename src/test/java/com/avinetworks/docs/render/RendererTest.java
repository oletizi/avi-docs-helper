package com.avinetworks.docs.render;

import com.avinetworks.docs.exec.ExecutorFactory;
import com.avinetworks.docs.repo.Repository;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RendererTest {
  private Renderer renderer;

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();
  private File repoDir;
  private ExecutorFactory execFactory;
  private Executor executor;
  private List<CommandLine> commands;
  private File binDir;
  private File outdir;

  @Before
  public void before() throws Exception {
    commands = new ArrayList<>();
    outdir = tmp.newFolder();
    repoDir = new File(outdir, "avi-docs");
    binDir = new File(repoDir, "bin");
    execFactory = mock(ExecutorFactory.class);
    executor = mock(Executor.class);
    when(execFactory.newExecutor()).thenReturn(executor);
    final ArgumentCaptor<CommandLine> cliCaptor = ArgumentCaptor.forClass(CommandLine.class);
    when(executor.execute(cliCaptor.capture())).thenAnswer(invocationOnMock -> {
      commands.add(cliCaptor.getValue());
      return 0;
    });
    renderer = new Renderer(repoDir, execFactory);
  }

  @Test
  public void testRender() throws Exception {
    renderer.render();
    verify(execFactory, times(1)).newExecutor();
    verify(executor, times(1)).setWorkingDirectory(binDir);

    assertEquals(1, commands.size());

    final CommandLine cmd = commands.get(0);
    verify(executor, times(1)).execute(cmd);

    assertEquals(Renderer.SHELL, cmd.getExecutable());
    assertArrayEquals(new String[] {Renderer.RENDER_COMMAND}, cmd.getArguments());
  }

  @Test
  @Ignore
  public void testRenderIT() throws Exception {
    outdir = new File(System.getProperty("user.home"), ".avi-docs-repo");
    repoDir = new File(outdir, "avi-docs");
    new Repository("https://github.com/oletizi/avi-docs.git", outdir, DefaultExecutor::new).cloneOrPull();
    renderer = new Renderer(repoDir, DefaultExecutor::new);
    renderer.render();
  }
}