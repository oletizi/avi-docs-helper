package com.avinetworks.docs.deploy;

import com.avinetworks.docs.exec.ExecutorFactory;
import org.apache.commons.exec.CommandLine;
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
  private ExecutorFactory execFactory;
  private Executor executor;
  private List<CommandLine> commands;
  private File binDir;

  @Before
  public void before() throws Exception {
    commands = new ArrayList<>();
    File outdir = tmp.newFolder();
    File repoDir = new File(outdir, "avi-docs");
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
    renderer.execute();
    verify(execFactory, times(1)).newExecutor();
    verify(executor, times(1)).setWorkingDirectory(binDir);

    assertEquals(1, commands.size());

    final CommandLine cmd = commands.get(0);
    verify(executor, times(1)).execute(cmd);

    assertEquals("bash", cmd.getExecutable());
    assertArrayEquals(new String[]{"render.sh"}, cmd.getArguments());
  }

  @Test
  @Ignore
  public void testRenderIT() throws Exception {
    //new RepositoryClone().cloneOrPull();
//    renderer = new Renderer();
//    renderer.execute();
  }
}