import java.util.Random;

public class JumpDecision {
	float x;
	float y;
	float power;
	
	final float maxPower = 6;
	
	//randomized constructor
	public JumpDecision() {
		Random rand = new Random();
		
		this.x = (rand.nextFloat() * 2) - 1; //random value between -1 and 1
		this.y = rand.nextFloat() * -1; //random value between 0 and -1;
		this.power = Game.grav + rand.nextFloat() * maxPower; //random number between grav and 10 + grav;
		if (this.power > maxPower)
			this.power = maxPower;
	}
	
	//basic constructor
	public JumpDecision(float x, float y, float power) {
		this.x = x;
		this.y = y;
		this.power = power;
	}
	
	//merging constructor
	public JumpDecision(JumpDecision decision1, JumpDecision decision2) {
		Random rand = new Random();
		//crossover
		if (rand.nextBoolean())
			this.x = decision1.x;
		else
			this.x = decision2.x;
		
		if (rand.nextBoolean())
			this.y = decision1.y;
		else
			this.y = decision2.y;
		
		if (rand.nextBoolean())
			this.power = decision1.power;
		else
			this.power = decision2.power;
		
		/* use avg crossover method
		this.x = (decision1.x + decision2.x) / 2;
		this.y = (decision1.y + decision2.y) / 2;
		this.power = (decision1.power + decision2.power) / 2;
		*/
		
		//mutation
		boolean slightMutate = rand.nextBoolean(); //0 for complete override, 1 for slight mutate
		if (slightMutate) {
			this.x = mutateValue(this.x, Game.mutationRate);
			this.y = mutateValue(this.y, Game.mutationRate);
			this.power = mutateValue(this.power, Game.mutationRate);
		}
		else {
			if (rand.nextFloat() < Game.mutationRate)
				this.x = (rand.nextFloat() * 2) - 1; //random value between -1 and 1
			if (rand.nextFloat() < Game.mutationRate)
				this.y = rand.nextFloat() * -1; //random value between 0 and -1;
			if (rand.nextFloat() < Game.mutationRate)
				this.power = Game.grav + rand.nextFloat() * maxPower; //random number between grav and 10 + grav;
		}
		
		//constraining vals
		if (this.x > 1)
			this.x = 1;
		else if (this.x < -1)
			this.x = -1;
		if (this.y > 1)
			this.y = 1;
		else if (this.y < -1)
			this.y = -1;
		if (this.power > maxPower)
			this.power = maxPower;
		else if (this.power < 0)
			this.power = 0;
	}
	
	private float mutateValue(float val, float mutationRate) {
		//if generated randVal is < mutationRate, then mutate the value
		//returns the same val if not mutated, returns mutated val if did mutate
		Random rand = new Random();
		float randVal = rand.nextFloat();
		if (randVal < mutationRate) {
			boolean positiveMutation = rand.nextBoolean();
			if (positiveMutation)
				val += (val * mutationRate);
			else
				val -= (val * mutationRate);
		}
		
		return val;
	}
}
