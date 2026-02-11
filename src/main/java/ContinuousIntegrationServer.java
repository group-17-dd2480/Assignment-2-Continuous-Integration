    import javax.servlet.http.HttpServletRequest;
    import javax.servlet.http.HttpServletResponse;
    import javax.servlet.ServletException;

    import java.io.IOException;
    import java.nio.file.FileSystems;
    import java.nio.file.Path;
    import java.util.List;

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

        private final BuildHistory history;
        public ContinuousIntegrationServer() throws IOException {
            history = new BuildHistory(Path.of("ci-build-history"));
        }

        public void handle(String target,
                Request baseRequest,
                HttpServletRequest request,
                HttpServletResponse response)
                throws IOException, ServletException {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            //display build list or single build based on the target URL
            if (target.equals("/builds")) {
            showBuildList(response);
            return;
            } else if (target.startsWith("/builds/")) {
            showSingleBuild(target, response);
            return;
            }

            String owner = "group-17-dd2480";
            String repo  = "Assignment-2-Continuous-Integration";
            String sha   = "c4f4b9e22d33d5de33339cb91cd21c1a0d007bdb";
            System.out.println(target);
            try {
                Notifier notifier =
                        NotifierFactory.create(); 

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

            // continuous integration tasks
            // todo: check in which branch push happened, check commit message to avoid recursion
            // todo: clone that branch to local
            // done: compile the code
            // todo: push to branch, add unique commit message to avoid recursion
            // todo: delete local clone

            List<String> compileCommands = List.of("mvn", "clean", "compile");
            Path sourceDir = FileSystems.getDefault().getPath("");
            CiCompile ciCompile = new CiCompile(new MockCommandExecutorFactory(), compileCommands, sourceDir);// todo change to real program, instead of mockcommand
            try {
                CiCompile.CompileResult result = ciCompile.compile();
                if (result.isSuccess()) {
                    response.getWriter().println("Compilation successful<br><hr><p style=\"margin-left: 2em;\">");
                    response.getWriter().println(result.getOutput().replace("\n", "<br>"));
                    response.getWriter().println("</p><hr>");
                }
            } catch (IOException | InterruptedException e) {
                response.getWriter().println("Exception in compilation: " + e + "<br>");
            }


            //create a build entry when a POST request is made
            if(request.getMethod().equalsIgnoreCase("POST")){
                try{
                    String buildId = history.createBuild(sha, "success", "compile log", "test log");
                    response.getWriter().println("Created build: " + buildId + "<br>");
                } catch (IOException e){
                    response.getWriter().println("Failed to create build: " + e + "<br>");
                }
            }
            
            response.getWriter().println("CI job done");
        }

        private void showBuildList(HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1>Builds</h1>");
        for (String build : history.listBuilds()) {
            response.getWriter().println(
                "<a href=\"/builds/" + build + "\">" + build + "</a><br>");
        }
    }

        private void showSingleBuild(String target, HttpServletResponse response) throws IOException {
            String buildId = target.substring("/builds/".length());
            response.getWriter().println("<pre>");
            response.getWriter().println(history.getBuild(buildId));
            response.getWriter().println("</pre>");
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