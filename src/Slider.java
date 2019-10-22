import java.util.Random;

public class Slider extends Obstacle{
	
	float xdir;
	float ydir;
	float speed;
	
	public Slider(float x, float y, float w, float h) {
		super(x, y, w, h);
		
		Random rand = new Random();
		xdir = (rand.nextFloat() * 2) - 1; //random value between -1 and 1
		ydir = (rand.nextFloat() * 2) - 1; //random value between -1 and 1
		speed = 1 + rand.nextFloat() * 4; //random value between 1 and 5
	}
	
	public Slider(float x, float y, float w, float h, float xdir, float ydir, float speed) {
		super(x, y, w, h);
		
		this.xdir = xdir;
		this.ydir = ydir;
		this.speed = speed;
	}
	
	void update(boolean doDraw) {
		if (x + w > Game.width || x < 0 || y + h > Game.height || y < 0) {
			//reverse direction if out of bounds
			xdir *= -1;
			ydir *= -1;
		}
		
		x += xdir * speed;
		y += ydir * speed;
		
		if (doDraw)
			draw();
	}
	
	Slider cloneOb() {
		Slider clone = new Slider(x, y, w, h, xdir, ydir, speed);
		clone.lava = lava;
		
		return clone;
	}
}
