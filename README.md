![Code style verification](https://github.com/RecycleAppHSE/backend/workflows/Code%20style%20verification/badge.svg?branch=master)
![Release](https://github.com/RecycleAppHSE/backend/workflows/Release/badge.svg)
![Build](https://github.com/RecycleAppHSE/backend/workflows/Build/badge.svg?branch=master)

# How to run it?

Build docker image and start it at port 8080:

```
$ docker build . -t rcycle-app
$ docker run -e DB_URI="postgres://..." -p 8080:8080 rcycle-app
```

**DB_URI** is a standrart posrgres URI, sectiom [32.1.1.2](https://www.postgresql.org/docs/9.3/libpq-connect.html#LIBPQ-PARAMKEYWORDS)


# Code Style

The project uses Google Code Style. Run `./gradlew goJF` for automatic formatting.

# API specification

API specification can be found at [`API.md`](./API.md) file.
