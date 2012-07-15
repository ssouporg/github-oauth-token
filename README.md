github-oauth-token
==================

Simple appengine app to exchange a github OAuth temporary code for an access token.
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
- Try to run the app locally:
  - ant runserver
  - launch example commands above from command line to register a new entry and exchanging a token
  - Open a browser and go to the admin console: http://localhost:8080/_ah/admin - Datastore Viewer, to have a look at the lookup table
- Register a new app on Google App Engine and store its Application ID
- Upload the app the the newly created Application on Google App Engine, by running this command at command line: appcfg.sh update www
