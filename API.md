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

The received `user_id` is expected to be used for subsequent requests.

### 
