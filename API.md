Client communicate with server by sending HTTP 1.1 requests.

### New user creation endpoint

Request example:

```
GET /new_user
```

Response example:

```
HTTP/1.1 200 OK

{
  "user_id": 2123
}
```

The received `user_id` is expected to be used for subsequent requests as a value of "USER_ID" header.

### Collection points near

Request example:

```
GET /near_me?page=1&size=10

...
USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

{
  "collection_points": 
     [
      {
        "name": "Pokrovsky bulvar 2",
        "phone_number": "+74994001041",
        "web_site": "https://www.hse.ru/"
      }
     ]
}
```

