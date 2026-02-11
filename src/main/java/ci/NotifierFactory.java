package ci;

/**
 * Factory for creating Notifier instances.
 * Returns GitHubNotifier if GITHUB_TOKEN is set, otherwise MockNotifier for local testing.
 */
public class NotifierFactory {

    public static Notifier create() {
        String token = System.getenv("GITHUB_TOKEN");
        if (token != null && !token.isBlank()) {
            return new GitHubNotifier(token);
        }
        System.out.println("GITHUB_TOKEN not set, using MockNotifier");
        return new MockNotifier();
    }

    public static Notifier create(String token) {
        if (token != null && !token.isBlank()) {
            return new GitHubNotifier(token);
        }
        return new MockNotifier();
    }
}
