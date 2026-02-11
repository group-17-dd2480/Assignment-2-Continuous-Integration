package ci;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ci.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CiTestTest {
    @TempDir
    Path tempDir;
    
    @Test
    void testCiTestSuccess() {
        
        class SuccessFullMockCommandExecutor implements CommandExecutor {
            @Override
            public ExecResult execute(List<String> command, Path workDir) throws IOException, InterruptedException {
                // we just return a successful result with some dummy output.
                return new ExecResult(0, "Test successful");
            }
        }
        class MockCommandExecutorFactory implements CommandExecutorFactory {
            @Override
            public CommandExecutor create() {
                return new SuccessFullMockCommandExecutor();
            }
        }
        List<String> testCommands = List.of("java", "Main");
        Path sourceDir = tempDir;
        CiTest ciTest = new CiTest(new MockCommandExecutorFactory(), testCommands, sourceDir);
        try {
            CiTest.TestResult result = ciTest.runTests();
            assertTrue(result.isSuccess());
            assertEquals(0, result.getExitCode());
            assertEquals("Test successful", result.getOutput());
        } catch (IOException | InterruptedException e) {
            fail("Exception should not have been thrown");
        }
    }

    /**
     * This test verifies the failure path of the CiTest pipeline.
     * A mock CommandExecutor that simulates a failed test process is created,
     * along with a mock CommandExecutorFactory that returns this mock executor.
     * An instance of CiTest is created with these mocks, and the runTests method is called.
     * 
     */
    @Test
    void testCiTestFailure() {
        class FailingMockCommandExecutor implements CommandExecutor {
            @Override
            public ExecResult execute(List<String> command, Path workDir) throws IOException, InterruptedException {
                // we just return a failed result with some dummy output.
                return new ExecResult(1, "Test failed");
            }
        }
        class MockCommandExecutorFactory implements CommandExecutorFactory {
            @Override
            public CommandExecutor create() {
                return new FailingMockCommandExecutor();
            }
        }
        List<String> testCommands = List.of("java", "Main");
        Path sourceDir = tempDir;
        CiTest ciTest = new CiTest(new MockCommandExecutorFactory(), testCommands, sourceDir);
        try {
            CiTest.TestResult result = ciTest.runTests();
            assertFalse(result.isSuccess());
            assertEquals(1, result.getExitCode());
            assertEquals("Test failed", result.getOutput());
        } catch (IOException | InterruptedException e) {
            fail("Exception should not have been thrown");
        }
    }

}
