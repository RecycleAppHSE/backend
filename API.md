Client communicate with server by sending HTTP 1.1 requests.


<details><summary>MAP</summary>
<p>


## Collection points get all/specific id

Request example:

```
GET /point/:pointId

USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

{
    "points": [
    {
      "id": 21
      "name": "Pokrovsky bulvar 2",
      "address" : "179Б, улица Попова, Куета, Индустриальный район, Barnaul, городской округ Барнаул, Altai Krai, Siberian Federal District, 656000, Russia",
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
}

```
* if `pointId` is not specified, all is returned.
* `shedule` is null when schedule is not specified.

## Collection point corrections by point id

Request example:

```
GET /point/:pointId/corrections

USER_ID: 2123
```

Response example:
```
HTTP/1.1 200 OK
content-length: 1438

{
  "corrections" : [ {
    "id" : 6,
    "point_id" : 1,
    "field" : "works",
    "change_to" : "broken",
    "status" : "in-progress",
    "submit_time" : 1606697003,
    "like_count" : 0,
    "dislike_count" : 0
  }, {
    "id" : 1,
    "point_id" : 1,
    "field" : "recycle",
    "change_to" : [ "metal", "tetra_pack", "glass", "toxic", "paper" ],
    "status" : "applied",
    "submit_time" : 1606696933,
    "like_count" : 3,
    "dislike_count" : 0
  },...]
}
```

## Search collection points by name

Request example:

```
GET /search?q=Barnaul

USER_ID: 2123
```

Response example:
```
HTTP/1.1 200 OK
content-length: 6020

{
  "points" : [ {
    "id" : 84,
    "name" : " ТерИК ",
    "address" : "179Б, улица Попова, Куета, Индустриальный район, Barnaul, городской округ Барнаул, Altai Krai, Siberian Federal District, 656000, Russia",
    "phone_number" : null,
    "web_site" : null,
    "recycle" : [ "toxic", "other", "paper" ],
    "latitude" : 53.317977,
    "longitude" : 83.640004,
    "works" : "works_fine",
    "last_updated" : 1604352072,
    "schedule" : {
      "from" : null,
      "to" : null
    },
    "corrections_count" : 0
  },...] 
}
```

* Response set is always ten elements at max

## Suggest correction

Request example:

```
POST /correction/suggest

USER_ID: 2123

{
    "point_id": 11, 
    "field": "recycle",
    "change_to": ["metal", "glass", "plastic", "paper"]
}
```

Response example:

```
HTTP/1.1 200 OK

{
  "correction_id": 5
}
```

* Possible filed types are "recycle" and "works".

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
    "id": 5,
    "point_id": 11,
    "field": "recycle",
    "change_to": ["metal", "glass", "plastic", "paper"],
    "status": "in-progress",
    "submit_time": 1604352072,
    "like_count": 0,
    "dislike_count": 0
}
```

## Delete correction by id

Request example:
```
DELETE /correction/1

USER_ID: 2123
```

Response example:
```
HTTP/1.1 200 OK
content-length: 39

{
  "message" : "Successfully deleted"
}
```


## Like/dislike correction

Request example:

```
POST /correction/5/like

USER_ID: 2123

{
    "like": 1
}
```

* `like` == 1 means to like a correction
* `like` == 0 means to unlike/undislike
* `like` == -1 means to dislike a correction

Response example:

```
HTTP/1.1 200 OK

{ }
```

</p>
</details>

<details><summary>TIPS</summary>
<p>
    
## Get all tips collections

Request example:

```
GET /tip/collections

USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

{
  "collections" : [ {
    "id" : 1,
    "title" : "Как утилизировать правильно?",
    "tips_number" : 1
  }, {
    "id" : 2,
    "title" : "Типы контейнеров",
    "tips_number" : 0
  }, {
    "id" : 3,
    "title" : "Правила сортировки",
    "tips_number" : 0
  }, {
    "id" : 4,
    "title" : "Правила сортировки (часть 2)",
    "tips_number" : 0
  } ]
}
```
 
## Get all tips by collection id

Request example:

```
GET /tip/1

USER_ID: 2123
```

Response example:

```
HTTP/1.1 200 OK

{
  "tips" : [ {
    "id" : 1,
    "collection_id" : 1,
    "title" : "Раздельный сбор мусора: как правильно сортировать отходы для переработки?",
    "content" : "Утилизация мусора – переработка отходов с целью ликвидации или повторного применения. Чтобы улучшить экологию в стране, необходимо правильно организовать использование отходов. Строгий контроль в сфере переработки отходов позволяет избавиться от большого скопления мусора. Важной задачей контролирующих служб является защита окружающей среды и здоровья населения.\n\nВыброс мусора на открытой не оборудованной территории категорически запрещён!\n\nРуководствуясь правилами сбора мусора, все отходы сортируются по классам, а затем утилизируются.\n\nПлощадки для складирования отходов должны быть закрыты от природных осадков и ветров. Контроль ведётся над выбросами вредных веществ в атмосферу. Не допускается попадание ядовитых веществ в подземные воды. "
  } ]
}
```

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
  "collection_points_corrections_ids": 
    {
      "applied": [5,6,122],
      "in_progress": [1,7,12],
      "rejected": [4,87]
    }
}
```

</p>
</details>
