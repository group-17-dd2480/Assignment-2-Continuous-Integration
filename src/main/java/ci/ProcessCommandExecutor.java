package ci;
import java.util.List;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;


// ci/ProcessCommandExecutor.java
final class ProcessCommandExecutor implements CommandExecutor { 
    @Override
    public ExecResult execute(List<String> command, Path workDir) throws IOException, InterruptedException {
        // use ProcessBuilder to execute the command.
        ProcessBuilder builder = new ProcessBuilder(command);
        // use the working directory specified by workDir
        builder.directory(workDir.toFile());
        // redirect the error stream to the output stream, so we can capture all output in one place.
        builder.redirectErrorStream(true);
        // start the process and capture the output.
        Process process = builder.start();
        // read the output of the process. We use UTF-8 encoding to convert the bytes to a string.
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        // wait for the process to finish, but we also set a timeout of 5 minutes to avoid hanging indefinitely.
        boolean finished = process.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            return new ExecResult(-1, "Process timed out. Output:\n" + output);
        }
        int exitCode = process.exitValue();
        return new ExecResult(exitCode, output);
    }

}
