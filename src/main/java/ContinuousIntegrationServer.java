import ci.Notifier;
import ci.NotifierFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

// import ci.App;
// import ci.CiCompile;
import ci.*;

/**
 * Skeleton of a ContinuousIntegrationServer which acts as webhook
 * See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler {

    // OLD: regex-based payload parsing (kept for reference)
    // Pattern urlRegEx = Pattern.compile("(?:%22url%22:%22)(.*?)%22");
    // Pattern branchRegEx = Pattern.compile("(?:%22ref%22:%22refs%2fheads%2f)(.*?)%22");
    // Pattern shaRegEx = Pattern.compile("(?:%22after%22:%22)(.*?)%22");

    public void handle(String target,
            Request baseRequest,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        StringBuilder payload = new StringBuilder();

        BufferedReader reader = request.getReader();
        String line;

        while ((line = reader.readLine()) != null) {
            payload.append(line);
        }
        System.out.println(payload.toString());

        // decode webhook payload, supports www-form-urlencoded.
        String rawBody = payload.toString();
        String jsonBody;
        String encoded = rawBody.startsWith("payload=") ? rawBody.substring("payload=".length()) : rawBody;
        jsonBody = URLDecoder.decode(encoded, StandardCharsets.UTF_8);

        GitHubWebhookPayload hook = new GitHubWebhookPayload(jsonBody);
        String owner = hook.getLogin();
        String repo = hook.getRepositoryName();
        String sha = hook.getAfter();
        String branch = hook.getBranch();
        String cloneUrl = hook.getCloneUrl();

        try {
            Notifier notifier = NotifierFactory.create();
            boolean ok = true;

            notifier.setStatus(
                    owner,
                    repo,
                    sha,
                    ok ? "success" : "failure",
                    ok ? "Build & tests passed" : "Build or tests failed");

            System.out.println("GitHub status sent for " + sha);
            response.getWriter().println("GitHub status sent<br>");

        } catch (Exception e) {
            System.err.println("Failed to send GitHub status");
            e.printStackTrace();
            response.getWriter().println("Failed to send GitHub status: " + e + "<br>");
        }

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code

        // continuous integration tasks
        // done: start on push
        // todo: check commit message to avoid recursion
        // todo: extract url etc from payload
        // done: clone that branch to local
        // done: compile the code
        // todo: push to branch, add unique commit message to avoid recursion
        // todo: delete local clone

        try {
            // initiate clone object
            CiClone ciClone = new CiClone();
            // clone the code to local
            CiClone.CloneResult cloneResult = ciClone.gitCloneAndCheckout(cloneUrl, branch, sha);
            // handle clone failure
            if (!cloneResult.isSuccess()) {
                throw new Exception("git clone/checkout failed: " + cloneResult.getOutput());
            }
            Path cloneLocation = cloneResult.getClonedDirectory();

            List<String> compileCommands = List.of("mvn", "clean", "compile");
            // initiate compile object and compile the code
            CiCompile ciCompile = new CiCompile(new DefaultCommandExecutorFactory(), compileCommands, cloneLocation);
            // capture results
            CiCompile.CompileResult result = ciCompile.compile();
            if (result.isSuccess()) {
                response.getWriter().println("Compilation successful<br><hr><p style=\"margin-left: 2em;\">");
                response.getWriter().println(result.getOutput().replace("\n", "<br>"));
                response.getWriter().println("</p><hr>");
            }
            // Always clean up the cloned directory to avoid accumulating temp data.
            ciClone.cleanup(cloneLocation);
        } catch (Exception e) {
            response.getWriter().println("Exception in compilation: " + e + "<br>");
        }

        response.getWriter().println("CI job done");
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}

class SuccessFullMockCommandExecutor implements CommandExecutor {
    @Override
    public ExecResult execute(List<String> command, Path workDir) throws IOException, InterruptedException {
        // we just return a successful result with some dummy output.
        return new ExecResult(0, "The program output");
    }
}

class MockCommandExecutorFactory implements CommandExecutorFactory {
    @Override
    public CommandExecutor create() {
        return new SuccessFullMockCommandExecutor();
    }
}
