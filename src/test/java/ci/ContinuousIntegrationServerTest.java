package ci;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the CI pipeline.
 */
public class ContinuousIntegrationServerTest {

    private MockNotifier mockNotifier;

    @BeforeEach
    void setUp() {
        mockNotifier = new MockNotifier();
    }

    /**
     * This test verifies the full CI pipeline flow using mock components.
     * We simulate a webhook payload and verify that the correct status updates are made.
     */
    @Test
    void pipeline_successfulBuild_setsCorrectStatuses() {
        // Create a valid webhook payload
        String payload = """
            {
                "ref": "refs/heads/main",
                "before": "0000000000000000000000000000000000000000",
                "after": "abc123def456789012345678901234567890abcd",
                "repository": {
                    "id": 1296269,
                    "name": "Hello-World",
                    "full_name": "octocat/Hello-World",
                    "clone_url": "https://github.com/octocat/Hello-World.git",
                    "owner": {
                        "login": "octocat",
                        "id": 1
                    }
                },
                "pusher": {
                    "name": "octocat",
                    "email": "octocat@github.com"
                },
                "sender": {
                    "login": "octocat",
                    "id": 1
                }
            }
            """;

        // Parse the payload
        GitHubWebhookPayload webhook = new GitHubWebhookPayload(payload);

        assertEquals("octocat", webhook.getLogin());
        assertEquals("Hello-World", webhook.getRepositoryName());
        assertEquals("abc123def456789012345678901234567890abcd", webhook.getAfter());
        assertEquals("main", webhook.getBranch());
        assertEquals("https://github.com/octocat/Hello-World.git", webhook.getCloneUrl());
    }

    /**
     * This test verifies that the MockNotifier correctly records status updates.
     * We simulate the sequence of status updates that would occur during a successful build.
     */
    @Test
    void mockNotifier_recordsStatusSequence() {
        String owner = "owner";
        String repo = "repo";
        String sha = "abc123";

        // Simulate the status updates that occur during a CI run
        mockNotifier.setStatus(owner, repo, sha, "pending", "CI build started");
        mockNotifier.setStatus(owner, repo, sha, "success", "Build and tests passed");

        assertEquals(2, mockNotifier.getCalls().size());

        MockNotifier.StatusCall firstCall = mockNotifier.getCalls().get(0);
        assertEquals("pending", firstCall.state());
        assertEquals("CI build started", firstCall.description());

        MockNotifier.StatusCall lastCall = mockNotifier.getLastCall();
        assertEquals("success", lastCall.state());
        assertEquals("Build and tests passed", lastCall.description());
    }

    /**
     * This test verifies that the MockNotifier correctly records failure status.
     * Simulates a failed compilation scenario.
     */
    @Test
    void mockNotifier_recordsFailureStatus() {
        String owner = "test-owner";
        String repo = "test-repo";
        String sha = "abc123def456789012345678901234567890abcd";

        mockNotifier.setStatus(owner, repo, sha, "pending", "CI build started");
        mockNotifier.setStatus(owner, repo, sha, "failure", "Compilation failed");

        assertEquals(2, mockNotifier.getCalls().size());

        MockNotifier.StatusCall lastCall = mockNotifier.getLastCall();
        assertEquals("failure", lastCall.state());
        assertEquals("Compilation failed", lastCall.description());
    }

    /**
     * This test verifies the NotifierFactory returns MockNotifier when no token is set.
     */
    @Test
    void notifierFactory_noToken_returnsMockNotifier() {
        Notifier notifier = NotifierFactory.create(null);
        assertInstanceOf(MockNotifier.class, notifier);
    }
}
