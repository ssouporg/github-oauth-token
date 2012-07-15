github-oauth-token
==================

Simple appengine app to exchange a github OAuth temporary code for an access token

curl http://APPID.appspot.com/ -d "client_id=CLIENT_ID&client_secret=CLIENT_SECRET&origin=ORIGIN&security_code=SECURITY_CODE"

curl "http://APPID.appspot.com/?client_id=CLIENT_ID&code=TEMP_CODE&state=STATE"

