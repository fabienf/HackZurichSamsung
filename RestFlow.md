Required REST:

1. Upload file
  - Request: upload file
  - Response: 

  ```javascript
  {
    "id":"1",
    "file":"terms_and_conditions.pdf",
    "type":"manual",
    "parts":
    [
      {
        "id":"1",
        "name":"grand of rights",
        "description":"You and your Authorized Users ...",
        "pages":[1,3],
        "keys":["rights", "respect", "restrictions"]
      },
      {
        "id":"2",
        "name":"access to services",
        "description":"Only your employees, temporary employees, students, ...",
        "pages":[4,19],
        "keys":["employees", "subject", "online services", "materials"]
      },
      {
        "id":"3",
        "name":"limited warranty",
        "description":"Represents and warrants that it has the right and authority to make ...",
        "pages":[19,42],
        "keys":["warranty", "party", "expert", "exclusive"]
      },
      {
        "id":"4",
        "name":"miscellaneous",
        "description":"Charges and payment terms may be changed in accordance...",
        "pages":[42,48],
        "keys":["payment", "changed", "terminate", "communications", "authorized users"]
      }
    ]
  }
  ```
  
2. Print request
  * Request:
  ```javascript
  {
    "id":"1",
    "pages":
    [
      [1,5],
      [19,40]
    ]
  }
    ```
  * Response:
  File for downloading, which will be printed
