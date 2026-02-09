package ci;
import java.util.List;
import java.nio.file.Path;
import java.io.IOException;

// ci/CiCompile.java

/**
 * Runs a compile command in a given workspave using a command executor.
 * It captures the output, exit code, and success status of the compilation process.
 */
public class CiCompile {
  private final CommandExecutorFactory factory;
  private final List<String> compileCommands;
  private final Path sourceDir; 

  /**
   * Constructor for CiCompile.
   * 
   * @param factory A factory to create command executors.
   * @param compileCommands A list of commands to run for compilation.
   * @param sourceDir The workspace dir, where the compile commands will be executed.
   */
  public CiCompile(CommandExecutorFactory factory, List<String> compileCommands, Path sourceDir) { 
    this.factory = factory; 
    this.compileCommands = compileCommands;
    this.sourceDir = sourceDir;
  }

  /**
   * A class that gathers the compile result context.
   * It includes the exit code, output, and success status of the compilation process.
   * Any other exit code than 0 is considered a failure.
   */
  public static final class CompileResult {
    private final int exitCode;
    private final String output;
    private final boolean success;

    public CompileResult(ExecResult result) {
      this.exitCode = result.getExitCode();
      this.output = result.getOutput();
      this.success = (this.exitCode == 0);
    }

    public int getExitCode() {
      return exitCode;
    }

    public String getOutput() {
      return output;
    }

    public boolean isSuccess() {
      return success;
    }
  }

  /**
   * Executes the compile cmds in the specified dir.
   * @return A CompileResult containing the context of the compilation process.
   * @throws IOException 
   * @throws InterruptedException
   */
  public CompileResult compile() throws IOException, InterruptedException {
    CommandExecutor executor = factory.create();
    ExecResult result = executor.execute(compileCommands, sourceDir);
    return new CompileResult(result);
  }
}
