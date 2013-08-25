package com.adani.interaction.model;

/**
 * Class that represents the connection type.
 * Where a:
 * 	1 - Mention is denoted with a red colour.
 * 	2 - Retweet in denoted with a yellow colour.
 * 	3 - Hashtag denoted with a balck colour - HASH TAGS DONOT IMPLY DIRECT CONVERSATION AND THEY ARE NOT USED.
 * @author Imran
 *
 */
public class ConnectionType {

	public final static int RT_R = 216;
	public final static int RT_G = 255;
	public final static int RT_B = 61;

	public final static int MENTION_R = 255;
	public final static int MENTION_G = 77;
	public final static int MENTION_B = 0;

	// Not used
	final static int HASH_R = 0;
	final static int HASH_G = 0;
	final static int HASH_B = 0;

	public String type;
 
	/**
	 * Create a connection with a specified type.
	 * @param type The type of connection, If it is a mention or retweet.
	 */
	public ConnectionType(String type) {
		this.type = type;
	}
}