Client communicate with server by sending HTTP 1.1 requests.


<details><summary>MAP</summary>
<p>


## Collection points near

Request example:

```
GET /point/:pointId

USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

[
  {
    "id": 21
    "name": "Pokrovsky bulvar 2",
    "phone_number": "+74994001041",
    "web_site": "https://www.hse.ru/",
    "recycle": ["metal", "glass", "plastic", "paper"],
    "latitude": 38.8951,
    "longitude": -77.0364,
    "works":  "broken" | "would_not_work" | "works_fine",
    "last_updated": 1604343073
    "schedule":{
        "from": "09:00",
        "to": "17:00"
     },
    "corrections_count": 2 
  }
]

```
* if `pointId` is not specified, all is returned.
* `shedule` is null when schedule is not specified.

## Suggest correction

Request example:

```
POST /correction/suggest

USER_ID: 2123

{
    "id": 21
    "name": "Контейнер ГК "Тайгер-Сибирь",
    "phone_number": "+74994001041",
    "web_site": "https://www.hse.ru/",
    "recycle": ["metal", "glass", "plastic", "paper"],
    "latitude": 38.8951,
    "longitude": -77.0364,
    "works": "works_fine",
    "last_updated": 1604343073
    "shedule":{
        "from": "09:00",
        "to": "17:00"
     },
    "corrections_count": 2 
}
```

Response example:

```
HTTP/1.1 200 OK

{
  "correction_id": 5
}
```

## Get correction by id

Request example:

```
GET /correction/5

USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

{
  "from": {
    "id": 21
    "name": "Pokrovsky bulvar 2",
    "phone_number": "+74994001041",
    "web_site": "https://www.hse.ru/",
    "recycle": ["metal", "glass", "plastic", "paper"],
    "latitude": 38.8951,
    "longitude": -77.0364,
    "works": "broken",
    "last_updated": 1604343073
    "shedule":{
        "from": "09:00",
        "to": "17:00"
     },
    "corrections_count": 2 
  },
  "to": {
    "id": 21
    "name": "Pokrovsky bulvar 2",
    "phone_number": "+74994001041",
    "web_site": "https://www.hse.ru/",
    "recycle": ["metal", "glass", "plastic", "paper"],
    "latitude": 38.8951,
    "longitude": -77.0364,
    "works": "works_fine",
    "last_updated": 1604343073
    "shedule":{
        "from": "09:00",
        "to": "17:00"
     },
    "corrections_count": 2 
  }
}
```

</p>
</details>

<details><summary>NEWS</summary>
<p>


## Get news

Request example:

```
GET /news?page=1&size=10

USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

{
  "news": [
    {
      "id": 12,
      "title": "City pollution ranking",
      "conent": "The polluted city is ...",
      "source": "https://www.forbes.ru/newsroom/obshchestvo/393811-eksperty-nazvali-samye-zagryaznennye-goroda-rossii"
    }
  ]
}
```

## Add news to favourite

Request example:

```
POST /news/12/add_to_favourites

USER_ID: 2123

{
  "news_id" : 56
}
```

Response example:

```
HTTP/1.1 200 OK
```

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

{}
```

## Get me

Request example:

```
GET /me

USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

{
  "name" : "John Smith",
  "photo_url" : "shorturl.at/ehsJ3",
  "favourite_news_ids" : [1, 56, 5544],
  "collection_points_corrections_ids": 
    {
      "approved": [5,6,122],
      "not_approved": [1,7,12]
    }
}
```



</p>
</details>
