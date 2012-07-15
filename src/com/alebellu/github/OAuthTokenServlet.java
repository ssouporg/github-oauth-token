package com.alebellu.github;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.*;

public class OAuthTokenServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(OAuthTokenServlet.class.getName());

    /**
    * Exchange a github token for a given client id and temporary code.
    *
    * @request_param client_id the client id.
    * @request_param code the temporary code.
    * @request_param state the github state parameter.
    */
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

        String client_id = req.getParameter("client_id");
        String code = req.getParameter("code");
        String state = req.getParameter("state");

        if ( client_id == null || code == null || state == null ) {
            resp.setContentType( "application/json" );
            resp.addHeader( "Access-Control-Allow-Origin", "*" );
            resp.getWriter().println( "{ error: 'Please specify client_id, code and state' }" );
            return;
        }

        Key clientIdKey = KeyFactory.createKey( "GitHubOAuthData", client_id );

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        try {
            // retrieve the client_secret from the datastore
            Entity gitHubOAuthData = datastore.get( clientIdKey );

            String client_secret = (String) gitHubOAuthData.getProperty( "client_secret" );
            String origin = (String) gitHubOAuthData.getProperty( "origin" );

            // exchange temporary code and state for an access_token with github
            String query = String.format( "client_id=%s&client_secret=%s&code=%s&state=%s", 
                                         URLEncoder.encode( client_id, "UTF-8" ), 
                                         URLEncoder.encode( client_secret, "UTF-8" ),
                                         URLEncoder.encode( code, "UTF-8" ),
                                         URLEncoder.encode( state, "UTF-8" )
                            );

            URL url = new URL( "https://github.com/login/oauth/access_token?" + query );
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String ght = reader.readLine();
            reader.close();

            resp.setContentType( "application/json" );
            resp.setCharacterEncoding( "utf-8" );
            if ( origin != null ) {
                resp.addHeader( "Access-Control-Allow-Origin", origin );
            }
            String json = "{\"" + ght.replaceAll( "&", "\", \"" ).replaceAll( "=", "\": \"" ) + "\"}";
            resp.getWriter().println( json );
        } catch ( Exception ex) {
            log.severe( ex.getMessage() );
        }
	}

    /**
     * Creates a new entry in the datastore for a given client id. Client secret must be supplied.
     *
     * @request_param client_id the client id.
     * @request_param client_secret the client secret.
     */
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {

        String securityCode = req.getParameter( "security_code" );
        String client_id = req.getParameter( "client_id" );
        String client_secret = req.getParameter( "client_secret" );
        String origin = req.getParameter( "origin" );

        if ( client_id == null || client_id.trim().length() == 0 || client_secret == null || client_secret.trim().length() == 0 ) {
            resp.setContentType( "application/json" );
            resp.getWriter().println( "{ error: 'Please specify client_id and client_secret' }" );
            return;
        }

        Key clientIdKey = KeyFactory.createKey( "GitHubOAuthData", client_id );

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        // we check if a security code has been stored for this instance of the app
        Key securityKey = KeyFactory.createKey( "SecurityCode", "securityCode" );
        Entity datastoreSecurityCode = null;
        try {
            // retrieve the security code from the datastore
            datastoreSecurityCode = datastore.get( securityKey );
        } catch (EntityNotFoundException ex) {
            // no security code was found,create a default one
            datastoreSecurityCode = new Entity( securityKey );
            datastoreSecurityCode.setProperty( "value", "please_change_me" );
            datastore.put( datastoreSecurityCode );

            log.warning( "A security code was not found, a default one was created. In order to prevent unauthorized modifications to GitHubOAuthData it is strongly suggested to modify the security code via the datastore viewer console." );
        }

        // security check : security code parameter must match the security code stored in the datastore
        if ( securityCode != null && securityCode.equals( datastoreSecurityCode.getProperty( "value" ) ) ) {
            if (client_secret != null) {
                // if a client secret is specified we go for an insert/update
                Entity newData = new Entity( clientIdKey );
                newData.setProperty( "client_secret", client_secret );
                if (origin != null ) {
                    newData.setProperty( "origin", origin );
                }
                datastore.put( newData );
            }
        }
	}
}
