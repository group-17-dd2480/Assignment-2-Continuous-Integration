package ci;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CiClone.
 */
public class CiCloneTest {

    private Path testDirectory;
    private CiClone ciClone;

    // Mock executor that records commands and returns configurable results
    static class MockCommandExecutor implements CommandExecutor {
        private final List<List<String>> executedCommands = new ArrayList<>();
        private final List<Path> executedPaths = new ArrayList<>();
        private ExecResult resultToReturn = new ExecResult(0, "success");

        @Override
        public ExecResult execute(List<String> command, Path workingDirectory) {
            executedCommands.add(new ArrayList<>(command));
            executedPaths.add(workingDirectory);
            return resultToReturn;
        }

        public void setResultToReturn(ExecResult result) {
            this.resultToReturn = result;
        }

        public List<List<String>> getExecutedCommands() {
            return executedCommands;
        }

        public List<Path> getExecutedPaths() {
            return executedPaths;
        }
    }

    static class MockCommandExecutorFactory implements CommandExecutorFactory {
        private final MockCommandExecutor executor = new MockCommandExecutor();

        @Override
        public CommandExecutor create() {
            return executor;
        }

        public MockCommandExecutor getExecutor() {
            return executor;
        }
    }

    private MockCommandExecutorFactory mockFactory;

    @BeforeEach
    void setUp() throws IOException {
        testDirectory = Files.createTempDirectory("git-test");
        mockFactory = new MockCommandExecutorFactory();
        ciClone = new CiClone(mockFactory, testDirectory);
    }

    @AfterEach
    void tearDown() {
        ciClone.cleanup(testDirectory);
    }

    @Test
    void gitCloneAndCheckout_constructsCorrectCloneCommand() throws IOException, InterruptedException {
        String url = "https://github.com/owner/repo.git";
        String branch = "main";
        String sha = "abc123";

        CiClone.CloneResult result = ciClone.gitCloneAndCheckout(url, branch, sha);

        assertTrue(result.isSuccess());
        List<List<String>> commands = mockFactory.getExecutor().getExecutedCommands();
        assertEquals(2, commands.size());

        // Verify clone command
        List<String> cloneCmd = commands.get(0);
        assertEquals(List.of("git", "clone", "--branch", "main", "--single-branch", url, sha), cloneCmd);

        // Verify checkout command
        List<String> checkoutCmd = commands.get(1);
        assertEquals(List.of("git", "checkout", sha), checkoutCmd);
    }

    @Test
    void gitCloneAndCheckout_cloneRunsInBaseDirectory() throws IOException, InterruptedException {
        String url = "https://github.com/owner/repo.git";
        String branch = "main";
        String sha = "abc123";

        ciClone.gitCloneAndCheckout(url, branch, sha);

        List<Path> paths = mockFactory.getExecutor().getExecutedPaths();
        assertEquals(testDirectory, paths.get(0)); // clone runs in base dir
    }

    @Test
    void gitCloneAndCheckout_checkoutRunsInClonedDirectory() throws IOException, InterruptedException {
        String url = "https://github.com/owner/repo.git";
        String branch = "main";
        String sha = "abc123";

        ciClone.gitCloneAndCheckout(url, branch, sha);

        List<Path> paths = mockFactory.getExecutor().getExecutedPaths();
        assertEquals(testDirectory.resolve(sha), paths.get(1)); // checkout runs in cloned dir
    }

    @Test
    void gitCloneAndCheckout_cloneFails_returnsFailure() throws IOException, InterruptedException {
        mockFactory.getExecutor().setResultToReturn(new ExecResult(1, "fatal: repository not found"));

        CiClone.CloneResult result = ciClone.gitCloneAndCheckout(
                "https://github.com/fake/repo.git", "main", "abc123");

        assertFalse(result.isSuccess());
        assertEquals(1, result.getExitCode());
        assertTrue(result.getOutput().contains("repository not found"));
    }

    @Test
    void gitCloneAndCheckout_returnsClonedDirectory() throws IOException, InterruptedException {
        String sha = "abc123";

        CiClone.CloneResult result = ciClone.gitCloneAndCheckout(
                "https://github.com/owner/repo.git", "main", sha);

        assertEquals(testDirectory.resolve(sha), result.getClonedDirectory());
    }

    @Test
    void cleanup_removesDirectory() throws IOException {
        Path dirToClean = testDirectory.resolve("to-clean");
        Files.createDirectories(dirToClean);
        Files.writeString(dirToClean.resolve("file.txt"), "content");

        ciClone.cleanup(dirToClean);

        assertFalse(Files.exists(dirToClean));
    }
}
