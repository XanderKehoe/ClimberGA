
public class Stationary extends Obstacle {
	//Stationary obstacle
	
	public Stationary(float x, float y, float w, float h) {
		super(x, y, w, h);
	}
	
	Stationary cloneOb() {
		Stationary clone = new Stationary(x, y, w, h);
		clone.lava = lava;
		
		return clone;
	}
}
