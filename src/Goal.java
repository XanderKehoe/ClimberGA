import org.lwjgl.opengl.GL11;

public class Goal {
	float x;
	float y;
	float w;
	float h;

	public Goal(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	void draw() {
		GL11.glColor3f(0,0,1);
		
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x+w, y);
        GL11.glVertex2f(x+w, y+h);
        GL11.glVertex2f(x, y+h);
        GL11.glEnd();
	}
	
	double distanceTo(float x, float y) {
		//get euclidean distance from goal;
		return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
	}
}