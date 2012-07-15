github-oauth-token
==================

Simple appengine app to exchange a github OAuth temporary code for an access token.
This is useful for client side javascript apps that need to access the GitHub v3 api,
to overcome browser's Cross Origin restrictions. See this blog entry from
Christopher Chedeau for more details: http://blog.vjeux.com/2012/javascript/github-oauth-login-browser-side.html

The app mantains a lookup table that associates client ids to client_secret and origin info.
The service offers the possibility to insert/upate rows in the lookup table; a security_code
is required to perform these operations.

Exchanging a temporary code for an access token
==================

A GET to the service, specifying a "client_id" contained in the lookup table and a pair "code"/"state"
returned by GitHub authorization page (https://github.com/login/oauth/authorize), will trigger
a call to the GitHub accessToken service (https://github.com/login/oauth/access_token) to exchange
the temporary code for an access token.

Example GET to exchange a GitHub temporary code for an access token:

```curl -i "http://APPID.appspot.com/?client_id=CLIENT_ID&code=TEMP_CODE&state=STATE"```

Updating the lookup table
==================

A POST to the service, specifying the "client_id", the "client_secret", the "origin" and the "security_code",
will trigger an insert/update to the lookup table for the given "client_id".

Example POST to insert/update an entry in the GitHub OAuth lookup table :

```curl http://APPID.appspot.com/ -d "client_id=CLIENT_ID&client_secret=CLIENT_SECRET&origin=ORIGIN&security_code=SECURITY_CODE"```

Deplying the app
==================

- Download and install Google AppEngine SDK for Java: https://developers.google.com/appengine/downloads
- Register a new app on Google App Engine and copy its application ID (APPID).
- Paste the APPID inside the <application> tag in the file src/WEB-INF/appengine-web.xml
- Recreate the war: ```ant war```
- Upload the code on the the newly created Application on Google App Engine: ```appcfg.sh update www```
- Launch a POST command to let the app create a Security Code in the datastore:
```curl http://APPID.appspot.com/ -d ""```
- Go to the Google Datastore Viewer console and change the "security_code" to a secure code

