package ci;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GitHubWebhookPayload parsing.
 */
public class WebhookPayloadTest {

    private static final String VALID_PUSH_PAYLOAD = """
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

    @Test
    void parseValidPayload_extractsAllFields() {
        GitHubWebhookPayload payload = new GitHubWebhookPayload(VALID_PUSH_PAYLOAD);

        assertEquals("refs/heads/main", payload.getRef());
        assertEquals("main", payload.getBranch());
        assertEquals("abc123def456789012345678901234567890abcd", payload.getAfter());
        assertEquals("https://github.com/octocat/Hello-World.git", payload.getCloneUrl());
        assertEquals("octocat", payload.getLogin());
        assertEquals("Hello-World", payload.getRepositoryName());
    }

    @Test
    void parsePayload_withFeatureBranch_extractsBranchName() {
        String payload = """
                {
                    "ref": "refs/heads/feature/webhook-parser",
                    "before": "0000000000000000000000000000000000000000",
                    "after": "1234567890123456789012345678901234567890",
                    "repository": {
                        "id": 12345,
                        "name": "test-repo",
                        "full_name": "owner/test-repo",
                        "clone_url": "https://github.com/owner/test-repo.git",
                        "owner": {
                            "login": "owner",
                            "id": 1
                        }
                    },
                    "sender": {
                        "login": "owner",
                        "id": 1
                    }
                }
                """;

        GitHubWebhookPayload parsed = new GitHubWebhookPayload(payload);
        assertEquals("feature/webhook-parser", parsed.getBranch());
    }

    @Test
    void parseMalformedJson_throwsException() {
        assertThrows(JSONException.class, () -> new GitHubWebhookPayload("not valid json"));
    }

    @Test
    void parseMissingRequiredField_throwsException() {
        String missingAfter = """
                {
                    "ref": "refs/heads/main",
                    "repository": {
                        "id": 12345,
                        "name": "my-repo",
                        "full_name": "owner/my-repo",
                        "clone_url": "https://github.com/owner/my-repo.git",
                        "owner": {
                            "login": "owner",
                            "id": 1
                        }
                    }
                }
                """;

        assertThrows(JSONException.class, () -> new GitHubWebhookPayload(missingAfter));
    }
}
