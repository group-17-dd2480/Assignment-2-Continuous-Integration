import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
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

    // "url": "https://api.github.com/users/group-17-dd2480",
    Pattern urlRegEx = Pattern.compile("(?:%22url%22:%22)(.*?)%22");
    // "ref": "refs/heads/13-p1-(sub-2)-server-pulls-code-from-GitHub",
    Pattern branchRegEx = Pattern.compile("(?:%22ref%22:%22refs%2fheads%2f)(.*?)%22");
    // "after": "c2679413db5317a3b88bcfd1124fc5a5dc0592db",
    Pattern shaRegEx = Pattern.compile("(?:%22after%22:%22)(.*?)%22");

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

        try {
            String token = System.getenv("GITHUB_TOKEN");

            GithubStatusNotifier notifier = new GithubStatusNotifier(token);

            String owner = "group-17-dd2480";
            String repo = "Assignment-2-Continuous-Integration";
            String sha = "c4f4b9e22d33d5de33339cb91cd21c1a0d007bdb";

            notifier.setStatus(
                    owner,
                    repo,
                    sha,
                    "success",
                    "P3: status set from CI server");

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
            GitService gitService = new GitService();
            String url, branch, sha;
            Matcher regExMatcher = urlRegEx.matcher(payload.toString());
            if (regExMatcher.find()) {
                url = regExMatcher.group(1);
            } else {
                throw new Exception("url is null");
            }
            regExMatcher = branchRegEx.matcher(payload.toString());
            if (regExMatcher.find()) {
                branch = regExMatcher.group(1);
            } else {
                throw new Exception("branch is null");
            }
            regExMatcher = shaRegEx.matcher(payload.toString());
            if (regExMatcher.find()) {
                sha = regExMatcher.group(1);
            } else {
                throw new Exception("sha is null");
            }

            Path cloneLocation = gitService.gitCloneAndCheckout(url, branch, sha);

            List<String> compileCommands = List.of("mvn", "clean", "compile");
            CiCompile ciCompile = new CiCompile(new DefaultCommandExecutorFactory(), compileCommands, cloneLocation);
            CiCompile.CompileResult result = ciCompile.compile();
            if (result.isSuccess()) {
                response.getWriter().println("Compilation successful<br><hr><p style=\"margin-left: 2em;\">");
                response.getWriter().println(result.getOutput().replace("\n", "<br>"));
                response.getWriter().println("</p><hr>");
            }
            gitService.cleanup(cloneLocation);
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