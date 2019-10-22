import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class GameLoop{
	
	public static double bestFitness = 0;
	public static JumpDecision[] bestDecisions = new JumpDecision[300];
	
	static ArrayList<Game> trainingSessions = new ArrayList<Game>();
	static int threadCount = 6; //total number of threads to dedicate to this program
	
	static long window;
	static int inputCooldown = 0;
	
	public static void main(String[] args)
	{
		ArrayList<Obstacle> original_obList = new ArrayList<Obstacle>();
		
		generateObList(original_obList, 100);
				
		for (int i = 0; i < bestDecisions.length - 1; i++)
			bestDecisions[i] = new JumpDecision();
				
				
		Game g = new Game(false, original_obList);
		window=g.init();
		
		setupThreads(original_obList);

		float time = (float)glfwGetTime();
		// Run the rendering loop until the user has attempted to close
		// the window
		while ( !glfwWindowShouldClose(window) ) {

			glfwPollEvents();
			float time2=(float)glfwGetTime();
			g.drawFrame(time2-time);
			glfwSwapBuffers(window);
			time=time2;
			
		}

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
	}
	
	public static boolean keyPressed(int x){
		return glfwGetKey(window, x) == GLFW_PRESS;
	}
	
	static void generateObList(ArrayList<Obstacle> original_obList, int size) {
		Stationary baseStation = new Stationary(0, Game.height - 10, 25, 10);
		original_obList.add(baseStation);
		
		Stationary setStation1 = new Stationary(35, Game.height - 20, 20, 5);
		original_obList.add(setStation1);
		Stationary setStation2 = new Stationary(60, Game.height - 25, 15, 5);
		original_obList.add(setStation2);
		
		
		//generation rules currently set to 2/3 sliders, and 1/3 lava
		for (int i = 0; i < size; i++) {
			Obstacle thisObstacle;
			Random rand = new Random();
			
			//determine what type of obstacle it will be
			float typeDecision = rand.nextFloat();
			if (typeDecision < .66) 
				thisObstacle = new Slider(100 + (rand.nextFloat() * (Game.width - 200)), 
						50 + rand.nextFloat() * (Game.height - 100), 
						10 + rand.nextFloat() * 25, 
						10);
			else
				thisObstacle = new Stationary(100 + (rand.nextFloat() * (Game.width - 200)), 
						50 + rand.nextFloat() * (Game.height - 100), 
						10 + rand.nextFloat() * 25, 
						10);
			
			//determine if it will be a lava obstacle
			float lavaDecision = rand.nextFloat();
			if (lavaDecision < .33)
				thisObstacle.lava = true;
			else
				thisObstacle.lava = false;
			
			original_obList.add(thisObstacle);
		}
		
		
		/*
		Slider slider1 = new Slider(80, Game.height - 30, 15, 5, .9f, -.1f, 3f);
		original_obList.add(slider1);
		Slider slider2 = new Slider(Game.width - 100, Game.height - 320, 15, 5, 0f, 1f, 2f);
		original_obList.add(slider2);
		*/
	}

	static void setupThreads(ArrayList<Obstacle> original_obList) {
		System.out.println("Prepping threads...");
		for (int i = 0; i < threadCount - 1; i++) { //-1 to save 1 thread for main program
			System.out.println("Thread: "+i);
			Game trainingSession = new Game(true, original_obList);
			Thread newThread = new Thread(trainingSession);
			newThread.start();
			
			trainingSessions.add(trainingSession);
		}
		System.out.println("All threads go!");
	}
}
