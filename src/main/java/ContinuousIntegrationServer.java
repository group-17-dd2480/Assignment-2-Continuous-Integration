import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
 
import java.io.IOException;
 
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

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

        System.out.println(target);
        try {
            String token = System.getenv("GITHUB_TOKEN");

            GithubStatusNotifier notifier =
                    new GithubStatusNotifier(token);

            String owner = "group-17-dd2480";
            String repo  = "Assignment-2-Continuous-Integration";
            String sha   = "c4f4b9e22d33d5de33339cb91cd21c1a0d007bdb";

            boolean ok = true;
            
            notifier.setStatus(
                    owner,
                    repo,
                    sha,
                    ok ? "success" : "failure",
                    ok ? "Build & tests passed" : "Build or tests failed"
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