/conf Directory 
==============

*This directory should contain any configuration files needed for jetty deployment.*

Types of files
--------------

* mail.json
* \*-ds.json


### mail.json

*A json file that configures a mail session and binds it in jndi.*

```json
{
  "jndiName": "java:/Mail",
  "username": "username only if required",
  "password": "password only if required",
  "properties": {
    "mail-session-prop": "value",
    "another-mail-prop": "value"
  }
}
```

### \*-ds.json

*A json file that configures and binds a datasource.*

```json
{
  "jndiName": "java:/customDS",
  "connectionUrl": "jdbc:ds://ds",
  "driverClass": "",
  "username": "",
  "password": "",
  "initSize": "",
  "minPoolSize": "",
  "maxPoolSize": "",
  "testSQL": ""
}
```
