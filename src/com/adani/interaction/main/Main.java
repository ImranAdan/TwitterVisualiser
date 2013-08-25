package com.adani.interaction.main;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import processing.core.PApplet;
import processing.core.PImage;

import com.adani.interaction.model.ConnectionType;
import com.adani.interaction.model.Participant;
import com.adani.interaction.twitter.TwitterProcessor;

/**
 * Main application class, that controls the rendering and updating 
 * of each participant object.
 * @author Imran
 *
 */
public class Main extends PApplet {
	
	static public void main(String args[]) {
		final String name = Main.class.getCanonicalName();
		PApplet.main(new String[] { "--bgcolor=#ECE9D8", name });
	}

	private static final long RELEASE_INTERVAL = 1000;
	private static final long PARTICIPANT_CANVAS_LIMIT = 50; // Max number of participants to be drawn on the canvas
	
	// Image Settings
	PImage settings_img;
	PImage rt_img;
	PImage rt_pressed_img;
	PImage mention_img;
	PImage mention_pressed_img;
	PImage spaceBar_img;
	
	private int rt_start_x;
	private int rt_start_y;
	private int mention_start_x;
	private int mention_start_y;

	
	boolean rt_pressed;
	boolean mention_pressed;
	boolean animationPaused;
	
	TwitterProcessor processor;
	List<Participant> participants;
	
	private long lastReleasedParticipant = System.currentTimeMillis();
	private boolean started;


	public void setup() {
		size(displayWidth-200, displayHeight - 200);
		frameRate(50);
		setUpModels();
		setUpSettings();
	}

	public void draw() { 
		background(184,219,148);
		drawSettingsPanel();
		if (System.currentTimeMillis() > lastReleasedParticipant + RELEASE_INTERVAL && participants.size() < PARTICIPANT_CANVAS_LIMIT) // Release participants at time intervals
				getNextParticipant();	
		visualiseConversations();
	}
	
	
	
	/**
	 * Set up the key models and variables of the application. This includes
	 * the participant collection and the twitter processor.
	 */
	private void setUpModels() {
		participants = new CopyOnWriteArrayList<Participant>();
		started = false;
		animationPaused = false;
		processor = new TwitterProcessor();
	}

	/**
	 * Set up the settings panel, this includes setting images and 
	 * panel variables.
	 */
	private void setUpSettings() {
		settings_img = loadImage("./res/setting.png");
		spaceBar_img = loadImage("./res/space.png");	
		rt_img = loadImage("./res/RT.png");
		rt_pressed_img = loadImage("./res/RT_pressed.png");
		mention_img = loadImage("./res/mention.png");
		mention_pressed_img= loadImage("./res/mention_pressed.png");
		rt_start_x = width/2 - 50;
		rt_start_y = 20;	
		mention_start_x = width/2 + 100;
		mention_start_y = 20;
		rt_pressed = true;
		mention_pressed = true;
	}
	
	

	/**
	 * Draw the setting panel that is located on 
	 * the top of the canvas.
	 */
	private void drawSettingsPanel() {
		image(settings_img, 0, 0, width, 0+100);
		image(spaceBar_img, 0, height - 75, width, 75);
		if(rt_pressed){
			image(rt_pressed_img, rt_start_x, rt_start_y, 100, 50);
		}else{
			image(rt_img, rt_start_x, rt_start_y, 100, 50);
		}
		
		if(mention_pressed){
			image(mention_pressed_img, mention_start_x, rt_start_y, 100, 50);
		}else{
			image(mention_img, mention_start_x, mention_start_y, 100, 50);
		}
	}
	
	
	
	/**
	 * Get the next participant to be added to the overall 
	 * collection of participants to be drawn on the canvas.
	 */
	private void getNextParticipant() {	
		started = true;
		Participant p = createParticipant();
		processor.addTweetTo(p, participants, width, height); // Add twitter data to the participant object
		lastReleasedParticipant = System.currentTimeMillis();
	}
	
	
	/**
	 * Visualise the conversation by drawing the 
	 * participant alongside their connections and 
	 * the respective relationships. The method also
	 * check the location. 
	 */
	private void visualiseConversations() {
		checkMouseHover();
		for(int i = 0; i<participants.size(); i++){
			if(participants.get(i).alive){
				participants.get(i).update();
				drawParticipant(participants.get(i));
				drawParticipantsRelationShip(participants.get(i));
			}else{
				participants.remove(i);
				i--;
			}
		}
	}
	

	/**
	 * Check the location of the mouse and if the location 
	 * of the mouse intersects with any participant, then draw
	 * their on the screen.
	 */
	private void checkMouseHover() {
		for (Participant participant : participants) {
			if (participant.intersects(mouseX, mouseY)) {
				textSize(10);
				fill(0, 0, 0);
				text(participant.tweet_str, participant.position.x+ Participant.RADIUS, participant.position.y+ Participant.RADIUS);

			}
			for (Entry<ConnectionType, Participant> entry : participant.connections.entrySet()) {
				if (entry.getValue().intersects(mouseX, mouseY)) {
					textSize(10);
					fill(255, 255, 255);
					text(entry.getValue().tweet_str,entry.getValue().position.x + Participant.RADIUS,entry.getValue().position.y + Participant.RADIUS);
				}
			}

		}
	}
	
	/**
	 * Draw the actual participant on the screen.
	 * @param participant The participant to be drawn.
	 */
	private void drawParticipant(Participant participant) {
		fill(participant.r, participant.g, participant.b);
		stroke(255, 255, 255);
		ellipse(participant.position.x, participant.position.y, Participant.RADIUS * 2, Participant.RADIUS * 2);
	}

	

	/**
	 * Draw a the respective relationship between a participant and its connections
	 * @param participant The relationship of the participant to be drawn
	 */
	private void drawParticipantsRelationShip(Participant participant) {
		if(participant.alive && participant.connections.size() > 0){
			for (Entry<ConnectionType, Participant> entry : participant.connections.entrySet()){		
				if(rt_pressed && mention_pressed){
				    if(entry.getKey().type.equals("@")){
				    	stroke(ConnectionType.MENTION_R, ConnectionType.MENTION_G, ConnectionType.MENTION_B);
				    	line(participant.position.x, participant.position.y, entry.getValue().position.x, entry.getValue().position.y);
				    	fill(entry.getValue().r, entry.getValue().g, entry.getValue().b);
				    	ellipse(entry.getValue().position.x, entry.getValue().position.y, Participant.RADIUS * 2, Participant.RADIUS * 2);
				    }
				    
				    if(entry.getKey().type.equals("RT")){
				    	stroke(ConnectionType.RT_R, ConnectionType.RT_G, ConnectionType.RT_B);
				    	line(participant.position.x, participant.position.y, entry.getValue().position.x, entry.getValue().position.y);
				    	fill(entry.getValue().r, entry.getValue().g, entry.getValue().b);
				    	ellipse(entry.getValue().position.x, entry.getValue().position.y, Participant.RADIUS * 2, Participant.RADIUS * 2);
				    }
			    	
				}
				if (mention_pressed && !rt_pressed){
				    if(entry.getKey().type.equals("@")){
				    	stroke(ConnectionType.MENTION_R, ConnectionType.MENTION_G, ConnectionType.MENTION_B);
				    	line(participant.position.x, participant.position.y, entry.getValue().position.x, entry.getValue().position.y);
				    	fill(entry.getValue().r, entry.getValue().g, entry.getValue().b);
				    	ellipse(entry.getValue().position.x, entry.getValue().position.y, Participant.RADIUS * 2, Participant.RADIUS * 2);
				    }
				}
				
				if(rt_pressed && !mention_pressed){
				    if(entry.getKey().type.equals("RT")){
				    	stroke(ConnectionType.RT_R, ConnectionType.RT_G, ConnectionType.RT_B);
				    	line(participant.position.x, participant.position.y, entry.getValue().position.x, entry.getValue().position.y);
				    	fill(entry.getValue().r, entry.getValue().g, entry.getValue().b);
				    	ellipse(entry.getValue().position.x, entry.getValue().position.y, Participant.RADIUS * 2, Participant.RADIUS * 2);
				    }
				}
			}
		}
	}

	

	/**
	 * Resolve the collision between two participants
	 * @param participant Participant 1
	 * @param participant2 Participant 2
	 */
	private void resolveIntersections (Participant participant, Participant participant2) {
		participant.velocity.x *= -1;
		participant2.velocity.x *= -1;
		participant.velocity.y *= -1;
		participant2.velocity.y *= -1;
	}
	
	/**
	 * Get a random number between a max and min range.
	 * @param max
	 * @param min
	 * @return
	 */
	private float between(float max, float min) {
		return new Random().nextFloat() * (max - min) + min;
	}
	
	/**
	 * Create a new participant.
	 * @return A participant object.
	 */
	private Participant createParticipant(){
		Participant p = new Participant(width, height);
		int r,g,b;
		r = (int) between(10, 255);
		g = (int) between(10, 255);
		b = (int) between(10, 255);
		p.r = r;
		p.g = g;
		p.b = b;
		return p;
	}
	
	/**
	 * Check if the space bar has been pressed to stop animation i.e. participant movement
	 */
	public void keyPressed(){
		if(key==' '){
			if(animationPaused){
				for(Participant participant: participants){
					participant.speed = 0;
					for (Entry<ConnectionType, Participant> entry : participant.connections.entrySet()){
						entry.getValue().speed = 0;
					}
				}
				Participant.SPEEDS = 0;
				animationPaused = false;
			}else{
				for(Participant participant: participants){
					participant.speed = 1;
					for (Entry<ConnectionType, Participant> entry : participant.connections.entrySet()){
						entry.getValue().speed = 1;
					}
				}
				Participant.SPEEDS = 1;
				animationPaused = true;
			}
		}
	}	
	
	/**
	 * Check if the user has pressed any of the buttons
	 */
	public void mousePressed(){
		if(mouseX >= rt_start_x && mouseX < rt_start_x + 100 && mouseY >= rt_start_y && mouseY < rt_start_x + 50){ 
			if(rt_pressed) rt_pressed = false;
			else rt_pressed = true;
		}	
		if(mouseX >= mention_start_x && mouseX < mention_start_x + 100 && mouseY >= mention_start_y && mouseY < mention_start_x + 50){ 
			if(mention_pressed) mention_pressed = false;
			else mention_pressed = true;
		}
	}
	
	/**
	 * Print Information to the terminal.
	 * @param info_message The information message String to be printed.
	 */
	public static void info(String info_message) {
		System.out.println("[INFO] " + info_message);
	}	
	
	
	/**
	 * Print error message to the screen
	 * @param error_message
	 */
	public static void error(String error_message) {
		System.err.println("[ERROR] " + error_message + "\n");
	}



}
