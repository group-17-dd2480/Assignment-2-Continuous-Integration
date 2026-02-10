# Assignment-2-Continuous-Integration
A minimal **webhook-based CI server in Java** for compiling, testing and notifying results.

## Skeleton reference
Usage details for the original CI skeleton are documented here:

```
https://github.com/KTH-DD2480/smallest-java-ci
```

## Start the webhook server
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