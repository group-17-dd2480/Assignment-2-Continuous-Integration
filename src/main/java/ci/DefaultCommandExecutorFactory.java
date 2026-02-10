package ci;

// ci/DefaultCommandExecutorFactory.java

/**
 * Factory for creating CommandExecutor instances.
 * This implementation creates ProcessCommandExecutor instances, 
 * which is the production way of executing commands.
 * 
 * This can also be used for executing other CI jobs such as running tests.
 * 
 */
public class DefaultCommandExecutorFactory implements CommandExecutorFactory {

    /**
     * Creates a new cmd executor instance.
     */
    @Override
    public CommandExecutor create() {
        return new ProcessCommandExecutor();
    }
}
