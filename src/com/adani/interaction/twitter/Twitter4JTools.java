package com.adani.interaction.twitter;

// some examples of using Twitter4J (rather than the QM database)
// (this will let you use Twitter's search API & other functionality)
// you MUST install Twitter4J from http://twitter4j.org in your Processing libraries folder

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import twitter4j.FilterQuery;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


// general tools
public class Twitter4JTools {

  // OAuth key & secret for the application "Interaction Design Coursework"
  private static final String consumerKey = "ocBqPlbl41tDSZcCwQ";
  private static final String consumerSecret = "nplA0l6c0zmWvY3fozaqN6Dz8nVq9pGNcFIhMPTKvY";
  
  // variables for private user OAuth data
  private static final String accessTokenFile = "access-token.txt";
  
  private static String pin = null;

  // initialise configuration
  public static Configuration init() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setOAuthConsumerKey(consumerKey);
    cb.setOAuthConsumerSecret(consumerSecret);
    Configuration config = cb.build();
    return config;
  }

  public static void setPin(String pin) {
    System.out.println("Set PIN " + pin);
    Twitter4JTools.pin = pin;
  }

  public static AccessToken getAccessToken(String path, Configuration config) {
    File tokenFile = new File(path, accessTokenFile);
    AccessToken at = null;
    if (tokenFile.exists()) {
      // read from file
      try {
        BufferedReader br = new BufferedReader(new FileReader(tokenFile));
        String token = br.readLine();
        String tokenSecret = br.readLine();
        br.close();
        // try to get token
        at = new AccessToken(token, tokenSecret);
      } 
      catch (Exception e) {
        // couldn't read file/get token? just move on
        e.printStackTrace();
        at = null;
      }
    }
    // if no success, redirect user to authenticate and get new token
    if (at == null) {
      try {
        Twitter tw = new TwitterFactory(config).getInstance();
        RequestToken requestToken = tw.getOAuthRequestToken();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (at == null) {


          // pop up a dialog box to give the URL and wait for the PIN
          JFrame frame = new JFrame("Twitter Authorisation");
          frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          frame.setLayout(new GridLayout(5,1));
          JTextField textField = new JTextField(10);
          textField.setText(requestToken.getAuthorizationURL());
          JLabel l1 = new JLabel("Open the following URL and grant access to your account:");
          l1.setLabelFor(textField);
          frame.add(l1);
          frame.add(textField);
          JTextField textField2 = new JTextField(10);
          JLabel l2 = new JLabel("Enter the PIN here and press RETURN:");
          l2.setLabelFor(textField2);
          frame.add(l2);
          frame.add(textField2);
          textField2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              Twitter4JTools.setPin(e.getActionCommand());
            }
          }
          );
          // Display the window.
          frame.pack();
          frame.setVisible(true);

          while (pin == null) {
            // wait ...
            Thread.sleep(1);
          }

          try {
            if (pin.length() > 0) {
              at = tw.getOAuthAccessToken(requestToken, pin);
            } 
            else {
              at = tw.getOAuthAccessToken();
            }
          } 
          catch (TwitterException te) {
            if (401 == te.getStatusCode()) {
              System.out.println("Unable to get the access token.");
            } 
            else {
              te.printStackTrace();
            }
          }
        }
        // write to file
        BufferedWriter bw = new BufferedWriter(new FileWriter(tokenFile));
        bw.write(at.getToken() + "\n");
        bw.write(at.getTokenSecret() + "\n");
        bw.close();
      } 
      catch (Exception e) {
        // couldn't write file? die
        e.printStackTrace();
        System.exit(0);
      }
    }
    return at;
  }
  
  // convenience method for getting an API instance
  public static Twitter getTwitter(Configuration config, AccessToken aToken) {
    return new TwitterFactory(config).getInstance(aToken);
  }

}

// a tool for getting tweets from the Streaming API

class Twitter4JStreamer {

  ArrayList tweets;
  private static final int MAX_TWEETS = 10;
  public boolean hasNew;

  public Twitter4JStreamer(Configuration config, AccessToken aToken, boolean london) {
    hasNew = false;
    tweets = new ArrayList();
    StatusListener listener = new TJStatusListener(this);
    TwitterStreamFactory tsf = new TwitterStreamFactory(config);
    TwitterStream twitterStream = tsf.getInstance(aToken);
    twitterStream.addListener(listener);
    if (london) {
      FilterQuery fq = new FilterQuery();
      fq.locations(new double[][] { 
        {
          -0.4, 51.3
        }
        , {
          0.4, 51.6
        }
      }
      );
      twitterStream.filter(fq);
    } 
    else {
      // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
      twitterStream.sample();
    }
  }

  public void addTweet(Status tweet) {
    tweets.add(0,tweet);
    hasNew = true;
    if (tweets.size() > MAX_TWEETS) {
      tweets.remove(MAX_TWEETS);
    }
  }

  public Status getLatestTweet() {
    return (Status) tweets.get(0);
  }

  public ArrayList getLatestTweets() {
    return tweets;
  }
}

// a listener for the Twitter4JStreamer
class TJStatusListener implements StatusListener {

  Twitter4JStreamer ts;

  public TJStatusListener(Twitter4JStreamer ts) {
    this.ts = ts;
  }

  public void onStatus(Status status) {
    ts.addTweet(status);
  }

  public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    // should really remove deleted tweets here ...
  }

  public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
  }

  public void onScrubGeo(long userId, long upToStatusId) {
    // should really remove deleted location information here ...
  }

  public void onStallWarning(StallWarning stallWarning) {
    // should really do something about stalls here ...
  }

  public void onException(Exception ex) {
    ex.printStackTrace();
  }
}

// a class for accessing the search method of the Twitter Search API
class Twitter4JSearcher {

  private Twitter twitter;

  public Twitter4JSearcher(Configuration config, AccessToken aToken) {
    twitter = new TwitterFactory(config).getInstance(aToken);
  }

  public QueryResult search(String searchTerm, int page) {
    Query query = new Query(searchTerm);
    // query.page(page); // Twitter4J now requires nextPage() for further pages
    try {
      return twitter.search(query);
    } 
    catch (TwitterException ex) {
      ex.printStackTrace();
    }
    return null;
  }
}
