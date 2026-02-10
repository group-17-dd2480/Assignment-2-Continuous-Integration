package ci;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// This is the helper class respons only for posting commit status to githib
public class GitHubNotifier implements Notifier {
    private final HttpClient client = HttpClient.newHttpClient(); // Used to send request to Github rest api
    private final String token;

    public GitHubNotifier(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Missing GITHUB_TOKEN environment variable");
        }
        this.token = token;
    }

    // Post commit status
    @Override
    public void setStatus(String owner, String repo, String sha,
                          String state, String description) throws Exception {

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
                .header("Authorization", "token " + token)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "dd2480-mini-ci")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        // Send request and capture respone 
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("GitHub status update failed: "
                    + resp.statusCode() + " body=" + resp.body());
        }
    }
}
