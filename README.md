# ClimberGA

A self-learning AI that teaches itself how to navigate a randomly generated complex obstacle course quickly by utilizing multi-threading.

Majority of OpenGL related code came from my Intro to Video Game Design course that our professor provided for us to use.

Each of the agents that I call 'Climbers' have a simple goal, reach the blue box (the goal) at the top right side of the screen. They learn through the use of a Genetic Algorithm (https://en.wikipedia.org/wiki/Genetic_algorithm). However, all of these Climbers perform their calculations in the background on seperate threads, and only the 'Best Climber' (the climber with the highest recorded fitness) is displayed in the program.

The platforms (both stationary and moving) I refer to as 'obstacles'. The red obstacles are 'lava' obstacles, if a climber touches a lava obstacle more than once without touching a normal obstacle inbetween, it will die. 

If you wish to see the other climbers, press the up arrow key. It will show you a rough outline of their behavior and the paths they're exploring
