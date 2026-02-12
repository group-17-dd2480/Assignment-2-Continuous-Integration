package ci;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.time.Instant;


public class BuildHistory {
    private final Path baseDir;

    /**
     * Stores build results on disk.
     * Each build is stored as a text file and persists across server restarts.
     */
    public BuildHistory(Path baseDir) throws IOException {
        this.baseDir = baseDir;
        Files.createDirectories(baseDir);
    }

    /**
     * Creates a new build record.
     *
     * @param commitSha commit identifier
     * @param state build state (success/failure)
     * @param compileLog compilation output
     * @param testLog test output
     * @return unique build id (file name)
     */

    
    public String createBuild(String commitSha,String state,String compileLog,String testLog) throws IOException 
    {
        String timestamp = Instant.now().toString().replace(":", "-");
        String buildName = commitSha.substring(0, 7) + "-" + timestamp + ".txt";
        Path buildDir = baseDir.resolve(buildName);
        String buildInfo = 
                "Commit SHA: " + commitSha + "\n" +
                "Date: " + Instant.now() + "\n" +
                "State: " + state + "\n" +
                "-----Compile Log-----:\n" + 
                compileLog + "\n" +
                "-----Test Log-----:\n" + 
                testLog + "\n";
                
        Files.writeString(buildDir, buildInfo);
        return buildName;
    }

    /**
     * Lists all builds
     */
    public List<String> listBuilds() throws IOException {
    List<String> builds = new java.util.ArrayList<>();

    try (var paths = Files.list(baseDir)) {
        for (Path path : paths.toList()) {
            builds.add(path.getFileName().toString());
        }
    }
    return builds;
}

    /**
     * Reads a specific build file.
     */
    public String getBuild(String buildName) throws IOException {
        Path file = baseDir.resolve(buildName);
        return Files.readString(file);
    }
    
}