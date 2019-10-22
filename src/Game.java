import  org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.lang.*;


public class Game implements Runnable{
	public static int width=960;
	public static int height=640;
	private static String title="Game";
	
	long window;
	
	public static float mutationRate = 0.05f;
	public static final float grav = 0.2f;
	
	public ArrayList<Obstacle> obList;
	public ArrayList<Obstacle> original_obList;
	public ArrayList<Climber> climberList;
	public Climber bestClimber;
	public static Goal goal;
	
	public static boolean madeItToEnd = false;
	
	int N = 1000; //number of climbers
	int generation = 0;
	
	boolean doForcedDecisions = false;
	int forcedIndex = 0; //how many indices of the best climber to directly copy.
	
	boolean doDraw = false;
	
	public Game(boolean training, ArrayList<Obstacle> original_obList)
	{
		super();
		this.original_obList = original_obList;
		obList = new ArrayList<Obstacle>();
		
		obList = cloneObList();
		
		//if training thread, generate all the initial climbers
		//else just setup the one "overall best" climber
		if (training) {
			climberList = new ArrayList<Climber>();
			for (int i = 0; i < N; i++) {
				climberList.add(new Climber(10, height - 20, 8, 8));
			}
		}
		else {
			bestClimber = new Climber(10, height - 20, 8, 8);
			bestClimber.best = true;
		}
		
		goal = new Goal(width - 30, 10, 20, 20);
	}
	
	public boolean keyPressed(int x){
		return glfwGetKey(window, x) == GLFW_PRESS;
	}
	// returns window id
	public long init()
	{
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");
		
		window = glfwCreateWindow(width, height, title, NULL, NULL);
		

		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		//set up OpenGL
		glfwMakeContextCurrent(window);
		GL.createCapabilities();
		glfwSwapInterval(1);
		
		// screen clear is white (this could go in drawFrame if you wanted it to change
		glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
		
		// set projection to dimensions of window
        // set viewport to entire window
        GL11.glViewport(0,0,width,height);
         
        // set up orthographic projection to map world pixels to screen
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 1, -1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

		return window;
	}
	
	public void drawFrame(float delta)
	{
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
		
		for (Obstacle thisOb : obList){
			thisOb.update(true);
		}
		
		//keep bestClimber updated with the best found decisions
		bestClimber.decision = GameLoop.bestDecisions;
		bestClimber.update(true, obList);
		
		//reset game and climber if climber dies
		if (bestClimber.dead) {
			bestClimber.reset(false);
			obList = cloneObList();
		}
		
		goal.draw();
		
		//draw all climbers if doDraw is enabled
		if (doDraw)
			for (int i = 0; i < GameLoop.trainingSessions.size(); i++)
				for (int j = 0; j < GameLoop.trainingSessions.get(i).climberList.size(); j++)
					GameLoop.trainingSessions.get(i).climberList.get(j).draw();
		
		if (keyPressed(org.lwjgl.glfw.GLFW.GLFW_KEY_UP)){
			if (GameLoop.inputCooldown == 0){
				doDraw = !doDraw;
			}
			GameLoop.inputCooldown = 3;
		}
		else if (GameLoop.inputCooldown > 0)
			GameLoop.inputCooldown--;

		
	}
	
	public void train() {
		while (true) {
			for (Obstacle thisOb : obList){
				thisOb.update(false);
			}
			
			for (Climber thisClimber : climberList)
				thisClimber.update(false, obList);
			
			boolean allDead = true;
			for (Climber thisBoi : climberList)
				if (!thisBoi.dead) {
					allDead = false;
					break;
				}
			
			if (allDead) {
				evolveGeneration();
			}
		}
	}
	
	public ArrayList<Obstacle> cloneObList() {
		ArrayList<Obstacle> clone = new ArrayList<Obstacle>(original_obList.size());
		//deep clones the original obList to the temporal obList
		for (int i = 0; i < original_obList.size(); i++)
			clone.add(original_obList.get(i).cloneOb());
		
		return clone;
	}
	
	public void evolveGeneration() {
		generation++;
		
		ArrayList<Climber> nextGen = new ArrayList<Climber>(N);
		
		//elitism (add the best from this generation to next generation)
		double bestFitness = 0;
		int bestIndex = 0;
		double sumFitness = 0;
		for (int i = 0; i < climberList.size(); i++) {
			sumFitness += climberList.get(i).fitness;
			if (climberList.get(i).fitness > bestFitness) {
				bestFitness = climberList.get(i).fitness;
				bestIndex = i;
			}
		}
		
		double avgFitness = sumFitness / climberList.size();
		//System.out.println("\tAverage Fitness: "+avgFitness);
		//System.out.println("\tBest Fitness: " + bestFitness);
		//System.out.println("\tForced Index: " + forcedIndex);
		
		for (int i = 0; i < N - 1; i++) { //N - 1 to make room for elitism
			Climber parent1 = chooseOnWeight(climberList);
			Climber parent2 = chooseOnWeight(climberList);
			
			if (doForcedDecisions)
				for (int j = 0; j < forcedIndex; j++) {
					parent1.decision[j] = climberList.get(bestIndex).decision[j];
					parent2.decision[j] = climberList.get(bestIndex).decision[j];
				}
			
			Climber child = new Climber(10, height - 20, 8, 8, parent1, parent2);
			nextGen.add(child);
		}
		
		
		if ((generation % 15 == 0 || bestFitness / 10 > avgFitness) 
		&& (3*forcedIndex) / 4 < climberList.get(bestIndex).closestDecisionIndex
		&& doForcedDecisions)
			forcedIndex++;
		
		Climber bestClimber;
		
		//if this climber is better than overall best climber, make this the new best climber
		//else put the overall best climber into the nextGen
		if (climberList.get(bestIndex).fitness > GameLoop.bestFitness) {
			GameLoop.bestDecisions = climberList.get(bestIndex).decision;
			GameLoop.bestFitness = climberList.get(bestIndex).fitness;
			bestClimber = climberList.get(bestIndex);
			
			System.out.println("\tNew Best Fitness: " + bestClimber.fitness);
		}
		else {
			bestClimber = new Climber(10, height - 20, 8, 8);
			bestClimber.decision = GameLoop.bestDecisions;
		}
		
		bestClimber.reset(false);
		nextGen.add(bestClimber);
		
		climberList = nextGen; //make the next generation happen
		obList = cloneObList();
		
	}
	
	public Climber chooseOnWeight(ArrayList<Climber> items) {
		//picks weighted random Climber based upon fitness
        double completeWeight = 0.0;
        for (Climber item : items)
            completeWeight += item.fitness;
        double r = Math.random() * completeWeight;
        double countWeight = 0.0;
        for (Climber item : items) {
            countWeight += item.fitness;
            if (countWeight >= r)
                return item;
        }
        return null;
    }
	

	@Override
	public void run() {
		train();
	}

}
