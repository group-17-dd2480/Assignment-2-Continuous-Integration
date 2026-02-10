package ci;

/**
 * ExecResult is a simple data class that gathers the result of executing a command.
 * There is no reason for it to be mutable, so we make it final and only provide getters.
 */
final class ExecResult {
    private final int exitCode;
    private final String output;
    
    ExecResult(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    int getExitCode() {
        return exitCode;
    }

    String getOutput() {
        return output;
    }
}
