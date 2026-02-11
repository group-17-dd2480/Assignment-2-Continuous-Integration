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

  
## Project Structure

The repository is organized as follows:

```text
src/
├── main/java/ci/
│   ├── ContinuousIntegrationServer.java  # Main entry point & HTTP handling
│   ├── GithubStatusNotifier.java         # Handles GitHub API notifications (P3)
│   ├── CiCompile.java                    # Handles compilation logic (P1)
│   ├── CiTest.java                       # Handles test execution logic (P2)
│   ├── GitService.java                   # Handles cloning and checkout
│   └── GitHubWebhookPayload.java         # JSON parsing for webhooks
│
└── test/java/ci/
    ├── CiCompileTest.java
    ├── CiTestTest.java
    ├── GitServiceTest.java
    ├── WebhookPayloadTest.java
    └── NotifierTest.java
```


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

* **Olivia:** Implemented GitHub commit status notification P3 and wrote the documentation.
* **Laasya:** 
* **Gabriel:**
* **Daniel:**
* **Sofia:**

## License

This project is licensed under the **MIT License**

## Start the webhook server
