package ci;

/**
 * Interface for sending build status notifications.
 */
public interface Notifier {

    void setStatus(String owner, String repo, String sha, String state, String description) throws Exception;
}
