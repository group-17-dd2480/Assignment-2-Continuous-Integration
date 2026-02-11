package ci;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GitService using our own repo.
 */
public class GitServiceTest {

    private Path testDirectory;
    private GitService gitService;

    @BeforeEach
    void setUp() throws IOException {
        testDirectory = Files.createTempDirectory("git-test");
        gitService = new GitService(testDirectory);
    }

    @AfterEach
    void tearDown() {
        gitService.cleanup(testDirectory);
    }

    @Test
    void gitCloneAndCheckout_validInput() throws IOException {
        // Clone a real public repository for integration testing
        String cloneUrl = "https://github.com/group-17-dd2480/Assignment-2-Continuous-Integration.git";
        String branch = "main";
        String commitSha = "b2c78cf5c916cf38dd3dc61219546203fe57a496";

        Path repoPath = gitService.gitCloneAndCheckout(cloneUrl, branch, commitSha);

        assertTrue(Files.exists(repoPath), "Repo directory should exist");
        assertTrue(Files.exists(repoPath.resolve("src/main/java/ci")), "ci package should exist");
        assertTrue(Files.exists(repoPath.resolve("README.md")), "README file should exist");
    }

    @Test
    void gitCloneAndCheckout_invalidUrl() {
        String invalidUrl = "https://github.com/fake/some-invalid-input-random-string-asdufkjdgflia.git";
        String branch = "nosuchbranch";
        String commitSha = "nosuchsha123";

        assertThrows(IOException.class, () ->
                gitService.gitCloneAndCheckout(invalidUrl, branch, commitSha)
        );
    }

    @Test
    void gitCloneAndCheckout_invalidBranch() {
        String cloneUrl = "https://github.com/group-17-dd2480/Assignment-2-Continuous-Integration.git";
        String invalidBranch = "nosuchbranch";
        String commitSha = "nosuchsha123";

        assertThrows(IOException.class, () ->
                gitService.gitCloneAndCheckout(cloneUrl, invalidBranch, commitSha)
        );
    }
}
