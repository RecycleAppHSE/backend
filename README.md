# How to run it?

Build docker image and start it at port 8080:

```
$ docker build . -t rcycle-app
$ docker run -p 8080:8080 rcycle-app
```

# Code Style

The project uses Google Code Style. Run `./gradlew goJF` for automatic formatting.

# API specification

API specification can be found at [`API.md`](./API.md) file.