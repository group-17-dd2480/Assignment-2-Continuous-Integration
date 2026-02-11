package ci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Service for cloning Git repositories and checking out specific commits.
 *
 */
public class CiClone {

    private final CommandExecutorFactory factory;
    private final Path baseDirectory;

    /**
     * Creates a CiClone with default settings.
     * Uses DefaultCommandExecutorFactory and clones to system temp directory.
     */
    public CiClone() {
        this.factory = new DefaultCommandExecutorFactory();
        this.baseDirectory = Path.of(System.getProperty("java.io.tmpdir"), "ci-builds");
    }

    /**
     * Creates a CiCLone with custom factory and directory.
     * Used for testing with mock executors.
     *
     * @param factory the factory to create command executors
     * @param testDir the directory where repositories will be cloned
     */
    public CiClone(CommandExecutorFactory factory, Path testDir) {
        this.factory = factory;
        this.baseDirectory = testDir;
    }

    /**
     * Result of a git clone and checkout operation.
     */
    public static final class CloneResult {
        private final int exitCode;
        private final String output;
        private final boolean success;
        private final Path clonedDirectory;

        /**
         * Creates a CloneResult from an ExecResult.
         *
         * @param result          the execution result from git command
         * @param clonedDirectory the path where the repo was cloned
         */
        public CloneResult(ExecResult result, Path clonedDirectory) {
            this.exitCode = result.getExitCode();
            this.output = result.getOutput();
            this.success = (this.exitCode == 0);
            this.clonedDirectory = clonedDirectory;
        }

        /** @return the exit code of the git command (0 = success) */
        public int getExitCode() {
            return exitCode;
        }

        /** @return the stdout/stderr output from the git command */
        public String getOutput() {
            return output;
        }

        /** @return true if the clone and checkout succeeded */
        public boolean isSuccess() {
            return success;
        }

        /** @return the path to the cloned repository directory */
        public Path getClonedDirectory() {
            return clonedDirectory;
        }
    }

    /**
     * Clones a repository and checks out a specific commit.
     *
     * @param url    the clone URL of the repository
     * @param branch the branch to clone
     * @param sha    the commit SHA to checkout
     * @return CloneResult containing success status, output, and cloned directory path
     * @throws IOException          if directory operations fail
     * @throws InterruptedException if the git process is interrupted
     */
    public CloneResult gitCloneAndCheckout(String url, String branch, String sha) throws IOException, InterruptedException {
        Path clonedDirectory = baseDirectory.resolve(sha);

        Files.createDirectories(baseDirectory);

        // If directory exists, it was probably corrupted, delete it
        if (Files.exists(clonedDirectory)) {
            try (var paths = Files.walk(clonedDirectory)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to delete " + path, e);
                            }
                        });
            }
        }

        CommandExecutor executor = factory.create();

        // git clone --branch <branch> --single-branch <url> <sha>
        List<String> cloneCommand = List.of("git", "clone", "--branch", branch, "--single-branch", url, sha);
        ExecResult cloneResult = executor.execute(cloneCommand, baseDirectory);
        if (cloneResult.getExitCode() != 0) {
            return new CloneResult(cloneResult, clonedDirectory);
        }

        // git checkout <sha>
        List<String> checkoutCommand = List.of("git", "checkout", sha);
        ExecResult checkoutResult = executor.execute(checkoutCommand, clonedDirectory);

        return new CloneResult(checkoutResult, clonedDirectory);
    }

    /**
     * Recursively deletes a directory.
     * Logs a warning on failure but does not throw.
     *
     * @param dir the directory to delete
     */
    public void cleanup(Path dir) {
        try {
            if (Files.exists(dir)) {
                try (var paths = Files.walk(dir)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    throw new RuntimeException("Failed to delete " + path, e);
                                }
                            });
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Failed to clean up " + dir + ": " + e.getMessage());
        }
    }
}
