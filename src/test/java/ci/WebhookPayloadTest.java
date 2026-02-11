package ci;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GitHubWebhookPayload parsing.
 */
public class WebhookPayloadTest {

    @Test
    void parseValidPayload_extractsAllFields() {
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
        GitHubWebhookPayload parsed = new GitHubWebhookPayload(payload);

        assertEquals("refs/heads/main", parsed.getRef());
        assertEquals("main", parsed.getBranch());
        assertEquals("abc123def456789012345678901234567890abcd", parsed.getAfter());
        assertEquals("https://github.com/octocat/Hello-World.git", parsed.getCloneUrl());
        assertEquals("octocat", parsed.getLogin());
        assertEquals("Hello-World", parsed.getRepositoryName());
    }

    @Test
    void parsePayload_withFeatureBranch_extractsBranchName() {
        String payload = """
            {
                "ref": "refs/heads/feature/something",
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

        GitHubWebhookPayload parsed = new GitHubWebhookPayload(payload);
        assertEquals("feature/something", parsed.getBranch());
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
                "before": "0000000000000000000000000000000000000000",
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

        assertThrows(JSONException.class, () -> new GitHubWebhookPayload(missingAfter));
    }
}
