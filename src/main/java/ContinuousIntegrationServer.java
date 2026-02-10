import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
 
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import ci.CiCompile;
import ci.CiTest;
import ci.DefaultCommandExecutorFactory;
import ci.GitHubWebhookPayload;
import ci.GitService;

/** 
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
*/
public class ContinuousIntegrationServer extends AbstractHandler
{
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        
        StringBuilder payloadBuilder = new StringBuilder();
        try (var reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                payloadBuilder.append(line);
            }
        }
        String payloadJson = payloadBuilder.toString();
        
        GitHubWebhookPayload webhookPayload = new GitHubWebhookPayload(payloadJson);
        String sha = webhookPayload.getAfter();  
        String repo = webhookPayload.getRepositoryName();
        String cloneUrl = webhookPayload.getCloneUrl();
        String branch = webhookPayload.getBranch(); 

        GitService gitService = new GitService();
        Path repoDirectory;
        try {
            repoDirectory = gitService.gitCloneAndCheckout(cloneUrl, branch, sha);
        } catch (IOException e) {
            response.getWriter().println("Failed to clone or checkout repo: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        CiCompile ciCompile = new CiCompile(
        new DefaultCommandExecutorFactory(),
        List.of("mvn", "compile"),
        repoDirectory); 
        
        CiCompile.CompileResult compileResult = null;
        CiTest.TestResult testResult = null;

        try {
            compileResult = ciCompile.compile();

            if (compileResult.isSuccess()) {
                CiTest ciTest = new CiTest(
                    new DefaultCommandExecutorFactory(),
                    List.of("java", "Main"),
                    repoDirectory
                );
                testResult = ciTest.runTests();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
            response.getWriter().println("CI job interrupted");
            return;

        } catch (IOException e) {
            response.getWriter().println("CI job failed: " + e.getMessage());
            return;
        }

        String state;
        String description;

        if (!compileResult.isSuccess()) {
            state = "failure";
            description = "Compilation failed";
        } else if (!testResult.isSuccess()) {
            state = "failure";
            description = "Tests failed";
        } else {
            state = "success";
            description = "Compilation and tests succeeded";
        }
        
        System.out.println(target);
        try {
            String token = System.getenv("GITHUB_TOKEN");

            GithubStatusNotifier notifier =
                    new GithubStatusNotifier(token);

            String owner = "group-17-dd2480";
            //String repo  = "Assignment-2-Continuous-Integration";
            //String sha   = "c4f4b9e22d33d5de33339cb91cd21c1a0d007bdb";

            notifier.setStatus(
                    owner,
                    repo,
                    sha,
                    state,
                    description
                    //"success",
                    //"P3: status set from CI server"
            );

            System.out.println("GitHub status sent for " + sha);

        } catch (Exception e) {
            System.err.println("Failed to send GitHub status");
            e.printStackTrace();
        }       

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code
        response.getWriter().println("CI job done");
    }
 
    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer()); 
        server.start();
        server.join();
    }
}