package ci;

import org.json.JSONObject;

/**
 * Parses GitHub webhook push event payloads.
 */
public class GitHubWebhookPayload {
    private final String ref;
    private final String after;
    private final String cloneUrl;
    private final String login;
    private final String repositoryName;

    /**
     * Constructs a WebhookPayload by parsing a GitHub push event JSON string.
     *
     * @param jsonPayload raw JSON payload from GitHub webhook
     * @throws org.json.JSONException if the JSON is malformed
     */
    public GitHubWebhookPayload(String jsonPayload) {
        JSONObject json = new JSONObject(jsonPayload);
        
        this.ref = json.getString("ref");
        this.after = json.getString("after");
        
        JSONObject repository = json.getJSONObject("repository");
        this.cloneUrl = repository.getString("clone_url");
        this.repositoryName = repository.getString("name");
        
        JSONObject ownerObj = repository.getJSONObject("owner");
        this.login = ownerObj.getString("login");
    }
    /**
     * Gets the full reference string (e.g., "refs/heads/main").
     *
     * @return The ref string.
     */
    public String getRef() {
        return ref;
    }
    /**
     * Gets the branch name from the ref.
     *
     * @return The branch name or the full ref if not a head.
     */
    public String getBranch() {
        if (ref != null && ref.startsWith("refs/heads/")) {
            return ref.substring("refs/heads/".length());
        }
        return ref;
    }

    /**
     * Returns the SHA of the commit that triggered the webhook.
     *
     * @return the 40-character commit SHA
     */
    public String getAfter() {
        return after;
    }
    /**
     * Returns the HTTPS URL used to clone the repository.
     *
     * @return The clone URL.
     */
    public String getCloneUrl() {
        return cloneUrl;
    }

    /**
     * Returns the repository owner's login name.
     *
     * @return the owner login
     */
    public String getLogin() {
        return login;
    }

    public String getRepositoryName() {
        return repositoryName;
    }
}
