import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

public class Climber {
	float x;
	float y;
	float w;
	float h;
	
	float xvel = 0;
	float yvel = 0;
	
	boolean airbourne = true;
	boolean hitLava = false;
	boolean dead = false;
	boolean best = false;
	
	JumpDecision[] decision = new JumpDecision[300];
	int decisionIndex = 0;
	
	double fitness = 0;
	double closestDistance = Double.POSITIVE_INFINITY;
	int closestDecisionIndex = 0;
	
	public Climber(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		for (int i = 0; i < decision.length - 1; i++)
			decision[i] = new JumpDecision();
	}
	public Climber(float x, float y, float w, float h, Climber parent1, Climber parent2) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
		//choose smallest decision index from parents
			//doing this so that future random data doesn't get merged and averaged for no reason
		int decisionLength = parent1.decisionIndex;
		if (parent2.closestDecisionIndex < decisionLength)
			decisionLength = parent2.closestDecisionIndex;
		
		for (int i = 0; i < decisionLength; i++)
			decision[i] = new JumpDecision(parent1.decision[i], parent2.decision[i]);
		for (int i = decisionLength; i < decision.length - 1; i++) {
			decision[i] = new JumpDecision();
		}
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
			
			updateFitness();
			
			if (doDraw)
				draw();
		}
	}
	
	void updateFitness() {
		
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
		{
			GL11.glColor3f(1,0,1);
			//System.out.println("\t\t"+x+" : "+y);
		}

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
		//check for fireball collision here later
	}
	
	void killAndCalcFitness() {
		dead = true;
		fitness = Math.pow((Math.sqrt(Math.pow(Game.width, 2) + Math.pow(Game.height, 2)))/closestDistance, 10);
		if (closestDistance < 20) { //made it to the end!!
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


