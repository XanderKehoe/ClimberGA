import org.lwjgl.opengl.GL11;

public class Obstacle {
	float x;
	float y;
	float w;
	float h;
	
	boolean lava;
	
	public Obstacle(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	void update(boolean doDraw) {
		if (doDraw)
			draw();
	}

	void draw() {
		if (lava)
			GL11.glColor3f(1,0,0);
		else
			GL11.glColor3f(0,0,0);
		
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x+w, y);
        GL11.glVertex2f(x+w, y+h);
        GL11.glVertex2f(x, y+h);
        GL11.glEnd();
	}
	
	
	Obstacle cloneOb() {
		Obstacle clone = new Obstacle(x, y, w, h);
		clone.lava = lava;
		
		return clone;
	}
}
