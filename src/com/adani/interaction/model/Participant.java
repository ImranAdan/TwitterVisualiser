package com.adani.interaction.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.adani.interaction.main.Main;

/**
 * Participants are individual circles that are to be drawn on the canvas.
 * The idea is that each participant is represented as a circle. Each circle is
 * connected to another set of circles. This means that a participant is connected to 
 * other sets of participants. This connection is done via a connection type.
 * There are two type of connections:
 * 
 *  1) An RT - Which is a retweet.
 *  2) A mention  - Which is @ 
 * 
 * @author Imran
 *
 */
public class Participant {

	// Participant class constants
	public static final float RADIUS = 10;
	public static float SPEEDS = 1;
	public static final long TIME_TO_LIVE = 15000;
	private static final Random RAND = new Random();
	
	// Object properties
	public long birth;
	public boolean alive;
	public boolean outOfBouds;
	
	// Geometric and visual properties
	public V2 position, velocity;
	public float container_width, container_height;
	public float speed;
	public int r, g, b;

	// Twitter properties
//	List<Participant> connectedTo;
	public String tweet_str = "";
	public Map<Long, String> tweetId_text;
	public Map<ConnectionType, Participant> connections;
	public String username = "";
	
	public Participant(float cw, float ch) {
		this.container_width = cw;
		this.container_height = ch;

		birth = System.currentTimeMillis();
		alive = true;
		outOfBouds = false;

		position = new V2(between(0, cw), between(100, ch-75));
		velocity = new V2(randomV(), randomV());
		r = (int) between(20, 255);
		g = (int) between(20, 255);
		b = (int) between(20, 255);
		speed = SPEEDS;
		
		connections = new ConcurrentHashMap<ConnectionType, Participant>();
		tweetId_text = new HashMap<Long, String>();
	}

	/**
	 * Update the properties of this participant.
	 * Only if they are alive. 
	 */
	public void update() {
		if(!isAlive()) return;
		move();
		for (Entry<ConnectionType, Participant> entry : connections.entrySet())
		   entry.getValue().update();
	}

	// Movment Functions 
	private void move() {
		resolveCollisions();
		position.x += velocity.x * speed;
		position.y += velocity.y * speed;
	}
	private void resolveCollisions() {	
		if (position.x - RADIUS <= 0 || position.x + RADIUS >= container_width)
			reverseX();
		if (position.y - RADIUS <= 100 || position.y + RADIUS >= container_height-75)
			reverseY();
	}
	public void reverseX() { velocity.x *= -1; }
	public void reverseY() { velocity.y *= -1; }

	// Random Number generator functions 
	private float between(float max, float min) {
		return RAND.nextFloat() * (max - min) + min;
	}

	private float randomV() {
		if ((int) Math.random()*10 > 5) return 1f;
		return -1f;
	}

	// intersection and boundary checking functions 
	public boolean intersects(Participant participant) {
		return Math.abs(this.position.subtract(participant.position).length()) <= RADIUS * 2;
	}
	
	public boolean intersects(int mouseX, int mouseY) {
		return Math.abs(new V2(mouseX-this.position.x, mouseY-this.position.y).length()) < RADIUS*2;
	}

	public boolean outOfBounds() {
		return (position.x - RADIUS <= 0 || position.x + RADIUS >= container_width ||position.y - RADIUS <= 0 || position.y + RADIUS >= container_height);
	}

	/**
	 * Return whether this participant is alive or not. If the 
	 * participant is not alive they are removed from the overall
	 * collection of the participants to be rendered. 
	 * @return Dead or alive.
	 */
	public boolean isAlive() {
	 if(System.currentTimeMillis() > birth + TIME_TO_LIVE)
		alive = false;
	 return alive;
	}


	public void printConnectionTypes(){
		for (Entry<ConnectionType, Participant> entry : connections.entrySet()){
			Main.info(entry.getKey().type + ":" + entry.getValue().tweet_str);
		}
	}

	/**
	 * Set up this current participant's connections based in analysing the 
	 * textual tweet within this participant object. 
	 * @param width The width of the container the participant is bounded to.
	 * @param height The height of the container the participant is bounded to.
	 */
	public void setUp(int width, int height) {
		String line = tweet_str.trim();
		String [] words = line.split(" ");
		for(int i = 0; i<words.length; i++){
			words[i].trim();
			if(!words[i].isEmpty() && words[i].length() >= 2){
				if(words[i].equalsIgnoreCase("RT")){
					if(i < words.length){
						if(words[i+1].charAt(0) == '@'){ // prone to exceptions need to fix!!!
							Main.info(words[i+1].substring(1)+ " was retweeted in..." + line);
							Participant p = new Participant(width, height);
//							p.velocity = this.velocity;
							ConnectionType c = new ConnectionType("RT");
							connections.put(c, p);
							if(username.length() > 0)
								connections.get(c).tweet_str = words[i+1].substring(1)+ " was retweeted by "+ username;
							else
								connections.get(c).tweet_str = words[i+1].substring(1)+ " was retweeted in..."+ line;
						}
					}
				}else{
					if(words[i].charAt(0) == '@'){
						Main.info(words[i].substring(1)+ " was mentioned in..." + line);
						Participant p = new Participant(width, height);
//						p.velocity = this.velocity;
						p.speed = this.speed;
						ConnectionType c = new ConnectionType("@");
						connections.put(c, p);
						if(username.length() > 0)
							connections.get(c).tweet_str = words[i].substring(1)+ " was mentioned in a conversation with " + username;
						else
							connections.get(c).tweet_str = words[i].substring(1)+ " was mentioned in..." + line;
					}	
				}
				
				Main.info(username);
				Main.info("FINISED...\n\n");
		}
		
//		printMentionedPeople(tweet_str);
//		for (Entry<Long, String> entry : tweetId_text.entrySet()){
//		    tweet_str =  entry.getValue();
//		    boolean print = false;
//			for (String s : tweet_str.split(" ")) {
//				s = s.trim();
//				if (s.length() > 0 && s.charAt(0) == '@') {
//					Participant p = new Participant(width, height);
//					p.velocity = this.velocity;
//					ConnectionType c = new ConnectionType("@");
//					connections.put(c, p);
//					connections.get(c).tweet_str = "Mentioned in.." + tweet_str;
//					print = true;
//					
//					if (s.length() > 0 && (s.contains("RT"))) {
//						print = true;
//						Participant d = p;
//						d.velocity = p.velocity;
//						d.speed = this.speed;
//						ConnectionType k = new ConnectionType("RT");
//						connections.put(k, d);
//						connections.get(k).tweet_str = "Retweeted AND also mentioned.." + tweet_str;
//					}
//				}
//				else if (s.length() > 0 && (s.contains("RT"))) {
//					print = true;
//					Participant p = new Participant(width, height);
//					p.velocity = this.velocity;
//					p.speed = this.speed;
//					ConnectionType c = new ConnectionType("RT");
//					connections.put(c, p);
//					connections.get(c).tweet_str = "Retweeted.." + tweet_str;
//				}
			//}
			//if (print) {
				//printConnectionTypes();

		}
	}
		//}
	


	private void printMentionedPeople(String line) {
		line.trim();
		String [] words = line.split(" ");
		for(int i = 0; i<words.length; i++){
			words[i].trim();
			if(!words[i].isEmpty() && words[i].length() > 3){
				if(words[i].charAt(0) == '@')
					if(i - 1 > 0)
						if(words[i-1].equalsIgnoreCase("RT")){
							Main.info(words[i].substring(1)+ " was retweeted in..." + line);
						}
					else{
						Main.info(words[i].substring(1)+ " was mentioned in..." + line);
					}
			}
		}
	}
	
	
	
	
}

