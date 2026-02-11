package ci;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Implementation of the Notifier interface using the GitHub REST API.
 * updates the commit status (success, failure, pending, error).
 */
public class GitHubNotifier implements Notifier {
    private final HttpClient client = HttpClient.newHttpClient(); // Used to send request to Github rest api
    private final String token;
    /**
     * Creates a new GitHubNotifier.
     *
     * @param token The GitHub Personal Access Token (PAT) with repo:status permissions.
     * @throws IllegalArgumentException If the token is null or blank.
     */
    public GitHubNotifier(String token) {
        // If no token we stop
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Missing GITHUB_TOKEN environment variable");
        }
        this.token = token;
    }

    /**
     * Sends a POST request to update the GitHub commit status.
     * * @param owner       The GitHub account owner.
     * @param repo        The repository name.
     * @param sha         The commit SHA ID.
     * @param state       The result (success, failure, or pending).
     * @param description A short summary of the result.
     * @throws Exception  If the HTTP request fails.
     */
    @Override
    public void setStatus(String owner, String repo, String sha,
                          String state, String description) throws Exception {
        // Create the URL based one repo owner, name comit
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/statuses/" + sha;
        
        // Json payload controls pass fail pending
        String json = "{"
                + "\"state\":\"" + state + "\","
                + "\"context\":\"group-17-ci\","
                + "\"description\":\"" + description + "\""
                + "}";
        // Build the HTTP request 
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                // Authentication, attach the token to the header
                .header("Authorization", "token " + token)
                // Content type, tell github we are sending json
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "dd2480-mini-ci")
                // Post to create/update status
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        // Send request and wait for respone 
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("GitHub status update failed: "
                    + resp.statusCode() + " body=" + resp.body());
        }
    }
}
