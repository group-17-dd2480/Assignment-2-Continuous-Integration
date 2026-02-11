package ci;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Service for cloning Git repositories and checking out specific commits.
 */
public class GitService {

    private final Path baseDirectory;

    public GitService() {
        this.baseDirectory = Path.of(System.getProperty("java.io.tmpdir"), "ci-builds");
    }

    //Separate constructor with a specifiable path. Specifically for testing purposes (Makes tests isolated so don't pollute system temp).
    public GitService(Path testDir) {
        this.baseDirectory = testDir;
    }

    public Path gitCloneAndCheckout(String url, String branch, String sha) throws IOException {
        Path clonedDirectory = baseDirectory.resolve(sha);

        try {
            Files.createDirectories(baseDirectory);

            // If a file exists, it was probably corrupted, delete it
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

            // git clone --branch <branch> --single-branch <url> <sha>
            ProcessBuilder cloneProcess = new ProcessBuilder(
                    "git", "clone", "--branch", branch, "--single-branch", url, sha);
            runGitCommand(cloneProcess, baseDirectory);

            // git checkout <sha>
            ProcessBuilder checkoutProcess = new ProcessBuilder("git", "checkout", sha);
            runGitCommand(checkoutProcess, clonedDirectory);

            return clonedDirectory;

        } catch (InterruptedException e) {
            throw new IOException("Failed to clone repository: " + e.getMessage(), e);
        }
    }

    private void runGitCommand(ProcessBuilder process, Path workingDir) throws IOException, InterruptedException {
        process.directory(workingDir.toFile());
        process.redirectErrorStream(true);
        Process command = process.start();
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(command.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }
        if (command.waitFor() != 0) {
            throw new IOException("git command failed: " + output);
        }
    }

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
