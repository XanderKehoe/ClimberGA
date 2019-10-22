import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

public class Climber {
	//x-y coords and width/height.
	float x;
	float y;
	float w;
	float h;
	
	//velocity x and y components
	float xvel = 0;
	float yvel = 0;
	
	boolean airbourne = true;
	boolean hitLava = false; //whether or not the last obstacle touched was a lava obstacle
	boolean dead = false; //fell off map
	boolean best = false; //used only to display different color for best Climber from previous generation
	
	JumpDecision[] decision = new JumpDecision[300]; //the 'DNA' for genetic algorithm, determines movement pattern.
	int decisionIndex = 0; //used to keep track of how far into 'jumping sequence' this climber is.
	
	double fitness = 0; //a measurement of how close this climber got to the goal.
	double closestDistance = Double.POSITIVE_INFINITY; //initializing to maximum possible value.
	int closestDecisionIndex = 0; //when fitness is maximized, record the index, anything beyond it will later be discarded
	
	//basic constructor
	public Climber(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		//initialing with random movement pattern.
		for (int i = 0; i < decision.length - 1; i++)
			decision[i] = new JumpDecision();
	}
	
	//a constructor for merging two climbers
	public Climber(float x, float y, float w, float h, Climber parent1, Climber parent2) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		//choose smallest decision index from parents
		//doing this so that future random data doesn't get merged and averaged for no reason
		int decisionLength = parent1.closestDecisionIndex;
		if (parent2.closestDecisionIndex < decisionLength)
			decisionLength = parent2.closestDecisionIndex;
		
		//merging parents movement pattern up to closestDecisionIndex
		for (int i = 0; i < decisionLength; i++)
			decision[i] = new JumpDecision(parent1.decision[i], parent2.decision[i]);
		
		//randomize the rest of the movement pattern
		for (int i = decisionLength; i < decision.length - 1; i++) 
			decision[i] = new JumpDecision();
		
	}
	
	void update(boolean doDraw, ArrayList<Obstacle> obList) {
		if (!dead) {
			if (airbourne) {
				int collisionIndex = collision(obList);
				if (collisionIndex != -1) {
					//about to collide
					Obstacle thisOb = obList.get(collisionIndex);
					
					if (y + h >= thisOb.y) {
						y = thisOb.y - h; //set position directly above the obstacle if above obstacle
					}
					
					yvel = 0;
					airbourne = false;
					
					if (thisOb.lava) {
						if (hitLava)
							killAndCalcFitness(); //hit lava twice is a row, must die
						else
							hitLava = true; //first jump on a lava obstacle
					}
					else
						hitLava = false; //reset hitLava
				}
				else {
					//no collision, do gravity n stuff
					x += xvel;
					y += yvel;
					yvel += Game.grav;
				}
			}
			
			else {
				if (decisionIndex < decision.length - 1)
					performJump();
				else 
					killAndCalcFitness();
			}
			
			checkDeath();
			
			updateClosestDecision();
			
			if (doDraw)
				draw();
		}
	}
	
	void updateClosestDecision() {
		double distanceToGoal = Game.goal.distanceTo(x, y);
		if (distanceToGoal < closestDistance) {
			closestDistance = distanceToGoal;
			closestDecisionIndex = decisionIndex;
		}
	}
	
	void draw() {
		if (!best)
			GL11.glColor3f(0,1,0);
		else
			GL11.glColor3f(1,0,1);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x+w, y);
        GL11.glVertex2f(x+w, y+h);
        GL11.glVertex2f(x, y+h);
        GL11.glEnd();
	}
	
	int collision(ArrayList<Obstacle> obList) {
		//return the index of the obstacle about to collide with
		//if no collision is found, return -1
		for (int i = 0; i < obList.size(); i++) {
			//if collision, return true;
			Obstacle thisOb = obList.get(i);
			if (thisOb.x < (x+xvel)+w && thisOb.x + thisOb.w > (x+xvel)
				&& thisOb.y < (y+yvel)+h && thisOb.y + thisOb.h > (y+yvel))
				return i;
		}
		return -1;
	}

	void performJump() {
		xvel = decision[decisionIndex].x * decision[decisionIndex].power;
		yvel = decision[decisionIndex].y * decision[decisionIndex].power;
		airbourne = true;
		decisionIndex++;
	}
	
	void checkDeath() {
		if (y > Game.height)
			killAndCalcFitness();
	}
	
	void killAndCalcFitness() {
		dead = true;
		fitness = Math.pow((Math.sqrt(Math.pow(Game.width, 2) + Math.pow(Game.height, 2)))/closestDistance, 10);
		if (closestDistance < 20) { //made it to the end!!
			//exponentially increase fitness inversely to amount of decisions had to be made to accomplish this
			//this challenges the AI to find a faster route to the end
			fitness *= Math.pow(closestDistance, (float) (decision.length - decisionIndex) / (float) 10);
			
			if (!Game.madeItToEnd){
				System.out.println("MADE IT TO THE END!!!");
				Game.madeItToEnd = true;
			}
			dead = true;
		}
	}
	
	void reset(boolean doClosestDecisionStuff) {
		
		//decisions that lead to being further away should be discarded
		if (doClosestDecisionStuff)
			for (int i = closestDecisionIndex; i < decision.length - 1; i++)
				decision[i] = new JumpDecision();
		
		x = 10;
		y = Game.height - 20;
		xvel = 0;
		yvel = 0;
		dead = false;
		decisionIndex = 0;
		airbourne = true;
		closestDistance = Double.POSITIVE_INFINITY;
		fitness = 0;
	}
}


