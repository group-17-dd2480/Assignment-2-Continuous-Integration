package ci;

import java.util.*;
import java.nio.file.Path;
import java.io.IOException;

// ci/CommandExecutor.java
public interface CommandExecutor {
  ExecResult execute(List<String> command, Path workingDirectory) throws IOException, InterruptedException;

}
