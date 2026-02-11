package ci;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Executes a test command in a workspace and captures the result.
 * Anything else than a non zero exit code is considered as failure.
 */
public class CiTest {
    private final CommandExecutorFactory factory;
    private final List<String> testCommands;
    private final Path sourceDir;

    /**
     * Creates a new CiTest instance.
     * @param factory the factory to create command executors
     * @param testCommands the commands to run tests
     * @param sourceDir the target dir
     */
    public CiTest(CommandExecutorFactory factory, List<String> testCommands, Path sourceDir) {
        this.factory = factory;
        this.testCommands = testCommands;
        this.sourceDir = sourceDir;
    }

    /**
     * Result of a test run.
     */
    public static final class TestResult {
        private final int exitCode;
        private final String output;
        private final boolean success;

        public TestResult(ExecResult result) {
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
     * Executes the test command in a target workspace.
     *
     * @return result of the test run
     * @throws IOException if the process cannot be started
     * @throws InterruptedException if the process is interrupted
     */
    public TestResult runTests() throws IOException, InterruptedException {
        CommandExecutor executor = factory.create();
        ExecResult result = executor.execute(testCommands, sourceDir);
        return new TestResult(result);
    }
}
