package ci;

/**
 * factory for creating command executors.
 * this makes it simple to swap out the implementation of command executor
 * for instance, we could create a MockCommandExecutor for testing purposes, 
 * and use that instead of the ProcessCommandExecutor in our tests.
 */

// ci/DefaultCommandExecutorFactory.java
public class DefaultCommandExecutorFactory implements CommandExecutorFactory {
    @Override
    public CommandExecutor create() {
        return new ProcessCommandExecutor();
    }
}
