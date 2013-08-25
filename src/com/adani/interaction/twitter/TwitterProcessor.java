package com.adani.interaction.twitter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.adani.interaction.main.Main;
import com.adani.interaction.model.Participant;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.examples.oauth.GetAccessToken;


/**
 * Twitter Processor is a custom class that makes use of various 
 * function that are based on the Twitter4J API. 
 * @author Imran
 *
 */
public class TwitterProcessor {

	private static final String GOOD_FORM_TWEET = "(.+(\\s|\\z))+"; //"(.+(\\s|\\z))*"; 
	final ConfigurationBuilder cb;
	final Configuration CONFIGURATION;
	final TwitterFactory tf;
	final Twitter twitter;
	Twitter4JStreamer streamer;

	/**
	 * Create a Twitter Processor object. The processor connects to twitter with the 
	 * applications properties. It also creates a twitter instance to be used in various 
	 * functions.
	 * @throws TwitterException
	 */
	public TwitterProcessor(){
		cb = new ConfigurationBuilder();
		
		// Need to register the application and generate keys using twitter dev site.  
		cb.setOAuthConsumerKey("***************");
		cb.setOAuthConsumerSecret("***************");
		cb.setOAuthAccessToken("***************");     
		cb.setOAuthAccessTokenSecret("***************");
		
		CONFIGURATION = cb.build();
		tf = new TwitterFactory(CONFIGURATION);
		twitter = tf.getInstance();
		try{
			streamer = new Twitter4JStreamer(CONFIGURATION,twitter.getOAuthAccessToken(), false);
		}catch(TwitterException e){
			Main.error("Unable to create a Twitter Processor " + e.toString());
			System.exit(-1);
		}
	}


	/**
	 * Get a collection of Tweets that contain a certain word.
	 * @param keyword The word to be queried for.
	 * @return A collection of tweets in String from.
	 */
	public List<String> getTweetsThatContain(String keyword) {
		List<String> tweets = new LinkedList<String>();
		Query query = new Query(keyword);
		QueryResult result;
		try {
			result = twitter.search(query);
			for (Status status : result.getTweets()) {
				Main.info("@" + status.getUser().getScreenName() + ":"+ status.getText());
				tweets.add(status.getUser().getScreenName() + ":"+ status.getText());
			}

		} catch (TwitterException e) {
			Main.error("Query returned errors");
		}
		return tweets;
	}

	/**
	 * Get a random Twitter Data from a stream. Use of function that is 
	 * provided in the Twitter4JTools class.
	 * @return A streamed data as a string form. 
	 */
	public String getTwitterStreamData() {
		if (streamer.hasNew) {
			Status status = streamer.getLatestTweet();
			User user = status.getUser();
			String streamData = "Tweet ID: " + status.getId() + 
							"\nText: "+ status.getText() + 
							"\nTimestamp: " + status.getCreatedAt() + 
							"\nLocation (GPS): "+ status.getGeoLocation();
			if (user != null) {
				streamData += "\nUsername: " + user.getScreenName()
						+ "\nUser ID: " + user.getId() + 
						"\nFull name " + user.getName() + 
						"\nLocation: " + user.getLocation();
			}

			return streamData;
		} else {
			Main.info("No good parsable tweet found");
			return "";
		}
	}
	

	
	/**
	 * This method incorporates twitter data into a participant object. The twitter
	 * data that is actually incorporates is the tweet itself only. This is
	 * achieved by parsing the string that is returned by getTwitterStreamData(). 
	 * If the parsing fails to get a tweet then this tweet is ignored and the participant 
	 * is then not be added to the overall collection of participants.
	 * 
	 * @param potential_participant
	 *            The participant to be added to the overall collection of participants.
	 * @param active_participants
	 *            The overall collection of current participants.
	 * @param screen_width
	 *            The width of the container this participant is bounded to.
	 *            This is the width of the canvas.
	 * @param screen_height
	 *            The height of the container this participant is bounded to.
	 *            This is the height of the canvas.
	 */
	public void addTweetTo(Participant potential_participant, List<Participant> active_participants, int screen_width, int screen_height) {	
		String tweet = getTwitterStreamData();
		Long tweetId = null;
		String text;
	
		if (tweet.length() > 0) {
			String [] lines = tweet.split("\n");
			if(!lines[1].matches(GOOD_FORM_TWEET)){
				Main.info("Bad From tweet, discared: " + lines[1]);
				return;
			}
			
			for (String words : lines) {
				if (words.split(":")[0].equalsIgnoreCase("Tweet ID")) {
					tweetId = Long.parseLong(words.split(":")[1].trim());
					for (Participant participant : active_participants) {  // check if this tweet ID is already in the list of already made participants, if so don't add this partciapnt.
						if (participant.tweetId_text.containsKey(tweetId))
							return;
					}
				}
	
				// Parse for the tweet as long as it has @ or RT then it is good to go.
				if (words.split(":")[0].equalsIgnoreCase("Text") && (words.split(":")[1].contains("@")|| words.split(":")[1].contains("RT") || words.split(":")[1].contains("#"))) { 
					text = words.split(":")[1];
					potential_participant = new Participant(screen_width, screen_height);
					Main.info("Successfully created new participant");
					potential_participant.tweetId_text.put(tweetId, text);
					Main.info("Participant tweeted: " + text);
					lookForUsername(potential_participant, tweet);
					
					if(potential_participant.username.length() > 0)
						potential_participant.tweet_str = potential_participant.username + " tweeted: " + text;
					else
						potential_participant.tweet_str = text;
					potential_participant.setUp(screen_width, screen_height);
					active_participants.add(potential_participant);
				}
			}
		}
	}




	private void lookForUsername(Participant p, String tweet) {
		for(String line: tweet.split("\n"))
			if(line.split(":")[0].equalsIgnoreCase("Username")){
				p.username = line.split(":")[1].trim();
		}
	}


	/**
	 * Return a random float between a maximum and a minimum ranges.
	 * @param max The max value.
	 * @param min The min Value.
	 * @return Random float.
	 */
	private float between(float max, float min) {
		return new Random().nextFloat() * (max - min) + min;
	}
}
