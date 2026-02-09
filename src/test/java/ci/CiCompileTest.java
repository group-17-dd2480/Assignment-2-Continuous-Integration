package ci;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.util.List;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CiCompileTest {

    @TempDir
    Path tempDir;

    /**
     * This test verifies the wiring and the happy path of the CiCompile pipeline.
     * We create a mock CommandExecutor that simulates a successful compilation process,
     * and mock a CommandExecutorFactory that returns this mock executor.
     * We then create an instance of CiCompile with these mocks and call the compile method.
     * 
     */
    @Test
    void testCiCompileSuccess() {
        
        class SuccessFullMockCommandExecutor implements CommandExecutor {
            @Override
            public ExecResult execute(List<String> command, Path workDir) throws IOException, InterruptedException {
                // we just return a successful result with some dummy output.
                return new ExecResult(0, "Compilation successful");
            }
        }
        class MockCommandExecutorFactory implements CommandExecutorFactory {
            @Override
            public CommandExecutor create() {
                return new SuccessFullMockCommandExecutor();
            }
        }
        List<String> compileCommands = List.of("javac", "Main.java");
        Path sourceDir = tempDir;
        CiCompile ciCompile = new CiCompile(new MockCommandExecutorFactory(), compileCommands, sourceDir);
        try {
            CiCompile.CompileResult result = ciCompile.compile();
            assertTrue(result.isSuccess());
            assertEquals(0, result.getExitCode());
            assertEquals("Compilation successful", result.getOutput());
        } catch (IOException | InterruptedException e) {
            fail("Exception should not have been thrown");
        }
    }

    /**
     * This test verifies the failure path of the CiCompile pipeline.
     * A mock CommandExecutor that simulates a failed compilation process is created,
     * along with a mock CommandExecutorFactory that returns this mock executor.
     * An instance of CiCompile is created with these mocks, and the compile method is called.
     * 
     */
    @Test
    void testCiCompileFailure() {
        class FailingMockCommandExecutor implements CommandExecutor {
            @Override
            public ExecResult execute(List<String> command, Path workDir) throws IOException, InterruptedException {
                // we just return a failed result with some dummy output.
                return new ExecResult(1, "Compilation failed");
            }
        }
        class MockCommandExecutorFactory implements CommandExecutorFactory {
            @Override
            public CommandExecutor create() {
                return new FailingMockCommandExecutor();
            }
        }
        List<String> compileCommands = List.of("javac", "Main.java");
        Path sourceDir = tempDir;
        CiCompile ciCompile = new CiCompile(new MockCommandExecutorFactory(), compileCommands, sourceDir);
        try {
            CiCompile.CompileResult result = ciCompile.compile();
            assertFalse(result.isSuccess());
            assertEquals(1, result.getExitCode());
            assertEquals("Compilation failed", result.getOutput());
        } catch (IOException | InterruptedException e) {
            fail("Exception should not have been thrown");
        }
    }

    /**
     * This helper method creates a simple java project that can be used for testing.
     */
    private Path writeSimpleJavaProject(Path root, String code) throws IOException {
        Path app = root.resolve("App.java");
        Files.writeString(app, code);
        return root;
    }

    private boolean isMavenAvailable() {
        try {
            Process process = new ProcessBuilder("mvn", "-v")
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    private boolean isJavacAvailable() {
        try {
            Process process = new ProcessBuilder("javac", "-version")
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    /**
     * Tests valid compile command with invalid code that contains two ";" instead of one.
     */
    @Test
    void testCiCompileWithJavacAndSimpleJavaProjectWithTypoInCode() {
        Assumptions.assumeTrue(isJavacAvailable(), "javac not available on PATH");
        try {
            // we create temp dir
            writeSimpleJavaProject(tempDir, "public class App { public static void main(String[] args) {System.out.println(\"Hello World\")};; }");
            List<String> compileCommands = List.of("javac", "App.java");
            CommandExecutorFactory factory = new DefaultCommandExecutorFactory();
            CiCompile ciCompile = new CiCompile(factory, compileCommands, tempDir);
            // we introduce a typo in the source code to cause a compilation error
            CiCompile.CompileResult result = ciCompile.compile();
            assertFalse(result.isSuccess());
            assertNotEquals(0, result.getExitCode());
        } catch (IOException | InterruptedException e) {
            fail("Exception should not have been thrown");
        }
    }

    /**
     * Test valid code with invalid compile command that does not exist.
     */
    @Test
    void testCiCompileWithSimpleJavaWithInvalidCompileCommand() {
        try {
            // we create temp dir
            writeSimpleJavaProject(tempDir, "public class App { public static void main(String[] args) {System.out.println(\"Hello World\");} }");
            List<String> compileCommands = List.of("invalidjavac", "App.java");
            CommandExecutorFactory factory = new DefaultCommandExecutorFactory();
            CiCompile ciCompile = new CiCompile(factory, compileCommands, tempDir);
            // we use an invalid compile command to cause an error
            CiCompile.CompileResult result = ciCompile.compile();
            assertFalse(result.isSuccess());
            assertNotEquals(0, result.getExitCode());
        } catch (IOException | InterruptedException e) {
            // we expect an exception to be thrown because the compile command is invalid
            assertTrue(e.getMessage().contains("invalidjavac"));
        }
    }

    /**
     * Test a valid compile command with valid code.
     */
    @Test
    void testCiCompileWithJavacAndSimpleJavaProject() {
        Assumptions.assumeTrue(isJavacAvailable(), "javac not available on PATH");
        try {
            // we create temp dir
            writeSimpleJavaProject(tempDir, "public class App { public static void main(String[] args) {System.out.println(\"Hello World\");} }");
            List<String> compileCommands = List.of("javac", "App.java");
            CommandExecutorFactory factory = new DefaultCommandExecutorFactory();
            CiCompile ciCompile = new CiCompile(factory, compileCommands, tempDir);
            CiCompile.CompileResult result = ciCompile.compile();
            assertTrue(result.isSuccess());
            assertEquals(0, result.getExitCode());
            assertTrue(result.getOutput().isEmpty() || result.getOutput().contains("Note:"));
        } catch (IOException | InterruptedException e) {
            fail("Exception should not have been thrown");
        }
    }

    /**
     * Test Maven compile on the current project workspace.
     */
    @Test
    void testCiCompileWithMavenOnCurrentProject() {
        // we assume that mvn is available or else we skip this test
        Assumptions.assumeTrue(isMavenAvailable(), "mvn not available on PATH");
        try {
            Path projectRoot = Path.of(System.getProperty("user.dir"));
            List<String> compileCommands = List.of("mvn", "compile");
            CommandExecutorFactory factory = new DefaultCommandExecutorFactory();
            CiCompile ciCompile = new CiCompile(factory, compileCommands, projectRoot);
            CiCompile.CompileResult result = ciCompile.compile();
            assertTrue(result.isSuccess());
            assertEquals(0, result.getExitCode());
        } catch (IOException | InterruptedException e) {
            fail("Exception should not have been thrown", e);
        }
    }

}
