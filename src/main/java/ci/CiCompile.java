package ci;
import java.util.List;
import java.nio.file.Path;
import java.io.IOException;

/**
 * This class is responsible for compiling the src code using provided commands.
 * It uses a command executor to compile the code and capture the output, exit code
 * and success status of the compilation process. The compile method executes the compile commands
 * in the specified src dir and returns a CompileResult object containing the results.
 */

// ci/CiCompile.java
public class CiCompile {
  private final CommandExecutorFactory factory;
  private final List<String> compileCommands;
  private final Path sourceDir; 

  public CiCompile(CommandExecutorFactory factory, List<String> compileCommands, Path sourceDir) { 
    this.factory = factory; 
    this.compileCommands = compileCommands;
    this.sourceDir = sourceDir;
  }

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

  public CompileResult compile() throws IOException, InterruptedException {
    CommandExecutor executor = factory.create();
    ExecResult result = executor.execute(compileCommands, sourceDir);
    return new CompileResult(result);
  }
}
