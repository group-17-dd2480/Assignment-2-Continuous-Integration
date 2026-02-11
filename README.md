# Assignment 2 - Continuous Integration Server - Group 17 KTH DD2480

A minimal **webhook-based CI server in Java** for compiling, testing, and notifying results. This project demonstrates the core principles of Continuous Integration by automating the build and test process upon GitHub events.

---

## Overview

This project implements a minimal Continuous Integration (CI) server that:
1.  **Receives** GitHub webhook events (HTTP POST).
2.  **Clones** and checks out the specific branch and commit.
3.  **Compiles** the project code (P1).
4.  **Executes** automated tests (P2).
5.  **Notifies** GitHub of the results via commit status (P3).

### Skeleton Reference
This project is inspired by the educational skeleton:
[https://github.com/KTH-DD2480/smallest-java-ci](https://github.com/KTH-DD2480/smallest-java-ci)

---
## Prerequisites & Dependencies
The project relies on the following dependencies (managed via Maven):

To build and run this project, you will need:

* **Java:** Version 17
* **Maven:** Version 3.6+
*  **Jetty** 
* **JUnit 5**
* **GitHub REST API** 
* **JSON (org.json)** 

  
## Core Features

## P1 – Compilation

### How it works
When the server receives a webhook from GitHub, it runs a compile step.
The compile command is executed and the exit code and output are captured. If the exit code is 0, the compilation is considered successful. Otherwise, it is considered a failure.
The compilation logic is implemented in the class CiCompile.
We have unit tests in CiCompileTest that check:
If compilation succeeds when commands return exit code 0, else compilation fails when commands return a non-zero exit code

### Where it is implemented
- `ci.ContinuousIntegrationServer` (wires the compile stage)
- `ci.CiCompile` (executes compile commands and returns `CompileResult`)
- `ci.CommandExecutor` / `ci.CommandExecutorFactory` 
---

## P2 – Testing

### How it works
After compilation, the server executes the project’s test commands. The output and exit code are captured. If all tests pass, the step is successful. If any test fails, the step is considered a failure.
The test execution logic is implemented in the class CiTest.
We have unit tests in CiTestTest that check:
Tests succeed when commands return exit code 0
Tests fail when commands return a non-zero exit code

### Where it is implemented
- `ci.CiTest` (executes test commands and returns `TestResult`)
- `ci.CommandExecutor` / `ci.CommandExecutorFactory` (abstraction for running commands)
---

## P3 – Notification 

### How it works
After compilation and testing, the server sends a commit status to GitHub using the GitHub REST API.
The status can be success, failure, pending, or error.
This is implemented in the class GithubStatusNotifier.
The GitHub Personal Access Token is read from the environment variable GITHUB_TOKEN.
We have unit tests in NotifierTest that verify the notification logic using mock notifiers.
When everything works, a green checkmark appears on the commit in GitHub with the context group-17-ci.

### Where it is implemented
- `GithubStatusNotifier` (builds and sends the HTTP request to GitHub)
- `ContinuousIntegrationServer` (calls `GithubStatusNotifier.setStatus(...)`)

## How to Build and Run

### 1. Build & Test
To verify the repository is in a valid state, run the following standard Maven commands in the root directory:

**Compile:**
```bash
mvn clean compile
```

### Run Unit Tests:
```bash
mvn test
```
### Package (Build JAR):
```bash
mvn package
```
### 2. Configure Environment (Required for P3)
To enable GitHub Status Notifications, you must provide a Personal Access Token with repo:status permissions.
```bash
export GITHUB_TOKEN=your_token_here
```
### 3. Start the Server
To run the server locally:
```bash
mvn dependency:copy-dependencies -DincludeScope=runtime
java -cp "target/classes:target/dependency/*" ci.ContinuousIntegrationServer
```
The server will start at: http://localhost:8080

Install ngrok:\
Windows:\
[ms-windows-store://pdp/?ProductId=9mvs1j51gmk6](ms-windows-store://pdp/?ProductId=9mvs1j51gmk6)\
Mac OS:\
`brew install ngrok`\
Linux:
``` bash
curl -sSL https://ngrok-agent.s3.amazonaws.com/ngrok.asc \
  | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null \
  && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" \
  | sudo tee /etc/apt/sources.list.d/ngrok.list \
  && sudo apt update \
  && sudo apt install ngrok
```

Run this command after installation:\
`ngrok config add-authtoken <redacted>`

Start the server (`ContinuousIntegrationServer.java`) so that it is running at [http://localhost:8080/](http://localhost:8080/)\
Then do this command:\
`ngrok http 8080`\

Then the server can be accessed here:\
[https://sociogenic-toya-reelingly.ngrok-free.dev/](https://sociogenic-toya-reelingly.ngrok-free.dev/)

Webhooks will be sent to that server

To see what data is sent\
[http://localhost:4040/](http://localhost:4040/)\
[https://ngrok.com/docs/agent/web-inspection-interface?ref=getting-started](https://ngrok.com/docs/agent/web-inspection-interface?ref=getting-started)

## Statement of Contributions

**Olivia:**
- Implemented GitHub commit status notification P3
- Wrote the documentation.
- GithubNotifier

**Laasya:** 

**Gabriel:**
- Environment setup (mvn, junit) & project structure initialization
- Build pipeline
- Test pipeline
- Javadoc
- Docs

**Daniel:**
- Webhook parsing + tests
- Repo cloning
- Notification
- Ci integration

**Sofia:**
- Documentation
- Refactoring

## SEMAT
Our team is currently in the Collaborating state. The mission of building  the CI server is defined, and responsibilities are divided among members. Team members understand their individual roles and communicate regularly through discord, GitHub issues, pull requests. We are working toward a shared goal and supporting each other when problems arise, the person with more knowlage Gabriel has taken a larger technical responsibility and often supports other members with design decisions and problem solving. The main obstacles to reaching the Performing state are final integration of all features and ensuring that the webhook reliably triggers the full pipeline. Once these are done and consistently working, everything should be working. 

## License

This project is licensed under the **MIT License**

## Start the webhook server
