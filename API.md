Client communicate with server by sending HTTP 1.1 requests.


<details><summary>MAP</summary>
<p>


## Collection points near

Request example:

```
GET /near_me?page=1&size=10&latitude=38.8951&longitude=-77.0364

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
        "recycle": ["metal", "glass", "plastic", "paper"]
      }
     ]
}
```


</p>
</details>

<details><summary>NEWS</summary>
<p>
</p>
</details>

<details><summary>ADVICES</summary>
<p>
</p>
</details>

<details><summary>USER PROFILE</summary>
<p>

## New user creation endpoint

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

## Change name

Request example:

```
POST /change_name

USER_ID: 2123

{
  "change_to" : "John Smith"
}
```

Response example:

```
HTTP/1.1 200 OK
```


## Add news to favourite

Request example:

```
POST /add_to_favorite

USER_ID: 2123

{
  "news_id" : 56
}
```

Response example:

```
HTTP/1.1 200 OK
```

## Get me

```
GET /me

USER_ID: 2123

{
  "name" : "John Smith",
  "favourite_news_ids" : [1, 56, 5544]
  "collection_points_corrections_ids": 
    {
      "approved": [5,6,122],
      "not-approved": [1,7,12]
    }
}
```

Response example:

```
HTTP/1.1 200 OK
```

</p>
</details>
