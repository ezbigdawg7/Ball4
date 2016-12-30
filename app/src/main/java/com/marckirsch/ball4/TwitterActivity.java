package com.marckirsch.ball4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class TwitterActivity extends AppCompatActivity {
    //*** START TWITTER ***//
    private static Twitter twitter;
    private static RequestToken requestToken;
    private AccessToken accessToken;
    private String TWITTER_CONSUMER_KEY;
    private String TWITTER_CONSUMER_SECRET;
    private String OAUTH_CALLBACK_SCHEME;
    private String TWITTER_CALLBACK_URL;
    private String PREF_KEY_OAUTH_TOKEN = "token";
    private String PREF_KEY_OAUTH_SECRET = "tokensecret";
    private String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    //*** END TWITTER ***//
    private Boolean doTweets = false;
    CharSequence[] accounts;
    SharedPreferences prefs;
//**** IMPORTANT NOTE - in order for Twitter to work, you must have an INTENT-FILTER within this activity in your Manifest File
    ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String PREFERENCE_NAME = getResources().getString(R.string.preference_name);
        String PREF_KEY_OAUTH_TOKEN = "token";
        String PREF_KEY_OAUTH_SECRET = "tokensecret";
        String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

        // Twitter oauth urls
        String URL_TWITTER_AUTH = "auth_url";
        String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
        String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

        TWITTER_CONSUMER_KEY = getResources().getString(R.string.twitter_consumer_key);
        TWITTER_CONSUMER_SECRET = getResources().getString(R.string.twitter_consumer_secret);
        OAUTH_CALLBACK_SCHEME = getResources().getString(R.string.oauth_callback_scheme);
        TWITTER_CALLBACK_URL = getResources().getString(R.string.twitter_callback_url)+OAUTH_CALLBACK_SCHEME;

        if (!isTwitterLoggedInAlready()) {
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                final String verifier = uri
                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
                new RetrieveAccessTokenTask().execute(verifier);
            }
        }
    }
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        PREF_KEY_TWITTER_LOGIN = getResources().getString(R.string.pref_key_twitter_login);
        return prefs.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }
    //**** START TWITTER CODE ******//
//	@Override
//	public void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		// Check if this is a callback from OAuth
//		if (!isTwitterLoggedInAlready()) {
//			Uri uri = intent.getData();
//			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
//				// oAuth verifier
//				final String verifier = uri
//						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
//				new RetrieveAccessTokenTask().execute(verifier);
//			}
//		}
//
//	}

    /* Responsible for retrieving access tokens from twitter */
    class RetrieveAccessTokenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String message = null;
            String verifier = params[0];
            try {
                // Get the access token
                TwitterActivity.this.accessToken = twitter
                        .getOAuthAccessToken(requestToken, verifier);
                // After getting access token, access token secret
                // store them in application preferences
                prefs.edit()
                        .putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken())
                        .commit();
                prefs.edit()
                        .putString(PREF_KEY_OAUTH_SECRET,
                                accessToken.getTokenSecret()).commit();
                // Store login status - true
                prefs.edit().putBoolean(PREF_KEY_TWITTER_LOGIN, true).commit();
                prefs.edit().putBoolean("twitteronoff", true).commit();
                doTweets = true;

            } catch (Exception e) {
                message = "OAuthMessageSignerException";
                e.printStackTrace();
            }
            return message;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Toast.makeText(TwitterActivity.this, result, Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(TwitterActivity.this, "Connected to Twitter",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */


    public void onClickAuthorize(View view)
    {
        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();


            Thread thread = new Thread(new Runnable(){
                @Override
                public void run() {
                    try {

                        requestToken = twitter
                                .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                        TwitterActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse(requestToken.getAuthenticationURL())));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        } else {
            // user already logged into twitter
            Toast.makeText(getApplicationContext(),
                    "Already Logged into twitter", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Function to update status
     * */
    class updateTwitterStatus extends AsyncTask<String, String, String> {

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            Log.d("Tweet Text", "> " + args[0]);
            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

                // Access Token
                String access_token = prefs.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = prefs.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                twitter4j.Status response = twitter.updateStatus(status);

                Log.d("Status", "> " + response.getText());
            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
                e.printStackTrace();
            }
            return status;
        }
        /**
         * After completing background task Dismiss the progress dialog and show
         * the data in UI Always use runOnUiThread(new Runnable()) to update UI
         * from background thread, otherwise you will get error
         * **/
        protected void onPostExecute(String result) {
            // updating UI from Background Thread
            Toast.makeText(TwitterActivity.this,
                    getText(R.string.goodpost)+ result, Toast.LENGTH_SHORT)
                    .show();

        }
    }

    public void onClickDeAuthorize(View view) {
        doTweets=false;
        prefs.edit().putBoolean("twitteronoff",false).commit();
        prefs.edit().putString("token", null).commit();
        prefs.edit().putString("tokensecret", null).commit();
        prefs.edit().putBoolean(PREF_KEY_TWITTER_LOGIN, false).commit();

        twitter = null;
    }

    public void onClickTwitter(View view) {
        Button twitterbutton = (Button)findViewById(R.id.twitterButton);
        if (twitterbutton.getText().equals(getText(R.string.twitterlogon_button)))
        {
            onClickAuthorize(view);
            twitterbutton.setText(getText(R.string.twitterlogoff_button));
        }
        else
        {
            onClickDeAuthorize(view);
            twitterbutton.setText(getText(R.string.twitterlogon_button));
        }

    }
    public void onClickPost(View view) {
        try
        {
            new updateTwitterStatus().execute(getText(R.string.twitterOn).toString());
        }catch (Exception ex)
        {

        }
    }
    public void homeClicked(View view)
    {
        Intent myIntent = new Intent(TwitterActivity.this, MainActivity.class);
        startActivity(myIntent);
        TwitterActivity.this.finish();
    }
    //**** END TWITTER CODE *****//
}

