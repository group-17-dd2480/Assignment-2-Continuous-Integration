import ci.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

/**
 * Skeleton of a ContinuousIntegrationServer which acts as webhook
 * See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler {

    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        // Read the webhook payload
        StringBuilder payloadBuilder = new StringBuilder();
        BufferedReader bufferedReader = request.getReader();
        String line = bufferedReader.readLine();
        while (line != null) {
            payloadBuilder.append(line);
            line = bufferedReader.readLine();
        }
        String payload = payloadBuilder.toString();

        if (payload.isEmpty()) {
            return;
        }

        System.out.println("Payload: " + payload.substring(0, Math.min(200, payload.length())));

        // Parse webhook payload
        GitHubWebhookPayload webhook = new GitHubWebhookPayload(payload);

        String owner = webhook.getLogin();
        String repo = webhook.getRepositoryName();
        String sha = webhook.getAfter();
        String branch = webhook.getBranch();
        String cloneUrl = webhook.getCloneUrl();

        Notifier notifier = NotifierFactory.create();
        CiClone ciClone = new CiClone();
        Path cloneLocation = null;

        try {
            // Set status to pending
            notifier.setStatus(owner, repo, sha, "pending", "Build started");

            // Clone repository
            CiClone.CloneResult cloneResult = ciClone.gitCloneAndCheckout(cloneUrl, branch, sha);
            if (!cloneResult.isSuccess()) {
                notifier.setStatus(owner, repo, sha, "failure", "Clone failed");
                response.getWriter().println("Clone failed: " + cloneResult.getOutput());
                return;
            }
            cloneLocation = cloneResult.getClonedDirectory();

            // Compile
            List<String> compileCommands = List.of("mvn", "compile", "-q");
            CiCompile ciCompile = new CiCompile(new DefaultCommandExecutorFactory(), compileCommands, cloneLocation);
            CiCompile.CompileResult compileResult = ciCompile.compile();

            if (!compileResult.isSuccess()) {
                notifier.setStatus(owner, repo, sha, "failure", "Compilation failed");
                response.getWriter().println("Compilation failed: " + compileResult.getOutput());
                return;
            }

            // Run tests
            List<String> testCommands = List.of("mvn", "test", "-q");
            CiTest ciTest = new CiTest(new DefaultCommandExecutorFactory(), testCommands, cloneLocation);
            CiTest.TestResult testResult = ciTest.runTests();

            if (!testResult.isSuccess()) {
                notifier.setStatus(owner, repo, sha, "failure", "Tests failed");
                response.getWriter().println("Tests failed: " + testResult.getOutput());
                return;
            }

            // Set success status
            notifier.setStatus(owner, repo, sha, "success", "Build and tests passed");
            response.getWriter().println("Build successful, all tests passed :D");

        } catch (Exception e) {
            System.err.println("Failed to send GitHub status");
            e.printStackTrace();
            try {
                notifier.setStatus(owner, repo, sha, "error", "CI error: " + e.getMessage());
            } catch (Exception notifyError) {
                System.err.println("Failed to send error status: " + notifyError.getMessage());
            }
            response.getWriter().println("CI error: " + e.getMessage());
        } finally {
            // Always cleanup
            if (cloneLocation != null) {
                ciClone.cleanup(cloneLocation);
            }
        }
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}

