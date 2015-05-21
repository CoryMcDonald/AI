// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)
import java.util.PriorityQueue;
import java.util.*;
import java.awt.*;
class CoryMcDonaldGenetic implements IAgent
{
	int iter;
	int index; // a temporary value used to pass values around
	boolean shadow;

	private Point[] forkedEnemyLocation = new Point[3];
	private Point[] prevEnemyLocation = new Point[3];


	public double geneticDefenderTravel = 2.9681700016639985;
	public double whenAttackEnemyAsFlag = 0.9875496984633174;
	public double whenFleeAsFlag = 0.08285741182279571;
	public double whenToDirectlyAttackAsAggressor = 0.9479174240537996;
	public boolean predictEnemyMovement = false;	
	CoryMcDonaldGenetic(double geneticDefenderTravel, double whenAttackEnemyAsFlag, double whenFleeAsFlag, double whenToDirectlyAttackAsAggressor, boolean predictEnemyMovement) {		
		this.geneticDefenderTravel = geneticDefenderTravel; 
		this.whenAttackEnemyAsFlag = whenAttackEnemyAsFlag; 
		this.whenFleeAsFlag = whenFleeAsFlag; 
		this.predictEnemyMovement = predictEnemyMovement;
		this.whenToDirectlyAttackAsAggressor = whenToDirectlyAttackAsAggressor;
		reset();
	}
	CoryMcDonaldGenetic(boolean shadow) {
		this.shadow = shadow;
		reset();
	}

	public void reset() {
		iter = 0;
		forkedEnemyLocation = new Point[3];
		prevEnemyLocation = new Point[3];
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}

	int numberOfDeadOpponents(Model m)
	{
		int numOfDead = 0;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) <= 0)
			{
				numOfDead++;
			}
		}
		return numOfDead;
	}

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
			m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
		}
	}


	void beAlternativeDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			int numOfOpponentsThirdWay = 0;
			int numOfOpponentsHalfWay = 0;
			for(int j = 0; j < m.getSpriteCountOpponent(); j++) {
				if(enemyX < Model.XMAX/1.75 )
				{
					numOfOpponentsThirdWay++;
				}
				if(enemyX < Model.XMAX/2)
				{
					numOfOpponentsHalfWay++;
				}
			}
			//Oh snap enemy is on my side of the board imma attack em
			if(numOfOpponentsHalfWay <= 1 && m.getX(i) <  Model.XMAX/geneticDefenderTravel && numOfOpponentsThirdWay <= 2 )
			{
				
				float myX = m.getX(i);
				float myY = m.getY(i);
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				walkTheDinosaur(m,i, new Point(enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON),(float)0));
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, myX - 10.0f * (myX - enemyX+m.getTravelSpeed(enemyX, enemyY)), myY - 10.0f * (myY - enemyY+m.getTravelSpeed(enemyX, enemyY))); 
			}else
			{			
				// Stay between the enemy and my flag
				m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));
			}

			// Throw boms if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.throwBomb(i, enemyX, enemyY);
		}
		else {
			// Guard the flag
			m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		}

		// If I don't have enough energy to throw a bomb, rest
		if(m.getEnergySelf(i) < Model.BOMB_COST)
			m.setDestination(i, m.getX(i), m.getY(i));

		// Try not to die
		avoidBombs(m, i);
	}


	void beFlagAttacker(Model m, int i) {		
		//Head for flag
		// Avoid opponents
		float myX = m.getX(i);
		float myY = m.getY(i);
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			float enemyDistanceFromFlag = sq_dist(enemyX, enemyY,  Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			float enemyDistance = sq_dist(enemyX, enemyY, myX, myY);
			// float enemyDistanceFromFlag = sq_dist(enemyX, enemyY, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
			float myDistanceFromFlag = sq_dist(myX, myY, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

			int deadEnemies = numberOfDeadOpponents(m);
			//If an enemy is closer to me than 3x from the flag I am under attack I should defend myself!
			if(((myDistanceFromFlag < enemyDistanceFromFlag && numberOfDeadOpponents(m) == 0) 
				|| (deadEnemies  > 0 && myDistanceFromFlag > enemyDistanceFromFlag  || m.getEnergyOpponent(index) < whenAttackEnemyAsFlag)) 
				&& m.getEnergySelf(i) > whenFleeAsFlag && deadEnemies != 3 )			
			{
				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;

				// m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
				walkTheDinosaur(m,i, new Point(enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON)));

				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				{
					if(forkedEnemyLocation[index] != null)
					{
						enemyX = forkedEnemyLocation[index].x;
						enemyY = forkedEnemyLocation[index].y;
					}
					m.throwBomb(i, enemyX, enemyY);
				}
			
			}
			else
			{
				float altUniverseEnemyDistance = sq_dist( forkedEnemyLocation[index].x,  forkedEnemyLocation[index].y, myX, myY);
				if ( m.getEnergySelf(i) < m.getEnergyOpponent(index) 
					&& enemyDistance > (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)
					&& altUniverseEnemyDistance > (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS))
				{
					m.setDestination(i, myX, myY); // Rest
				}else
				{
					if(iter > 75) //Don't want to go into the battle, even though it's the fastest path
					{
						walkTheDinosaur(m,i, new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));
					}
					else 
					{
						m.setDestination(i,Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
					}
					//Fleeing
					if(enemyDistance <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS))
					{
						m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY));
					}
				}

			}
		}else
		{
			m.setDestination(i,Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
		}

		// Shoot at the flag if I can hit it
		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}
		
		// Try not to die
		avoidBombs(m, i);
	}



	void beAggressor(Model m, int i) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)) {
				
				// Get close enough to throw a bomb at the enemy
				float enemyDistanceFromMyFlag = sq_dist(enemyX, enemyY,  Model.XFLAG, Model.YFLAG);
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;

				if( m.getEnergyOpponent(index) < whenToDirectlyAttackAsAggressor && m.getEnergySelf(i) > .5)
				{
					walkTheDinosaur(m,i, new Point(enemyX, enemyY));
				}else
				{
					walkTheDinosaur(m,i, new Point(enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON),(float)0));
				}

				//If the enemy is close to the flag let's not predict where he's going to go
				if(forkedEnemyLocation[index] != null 
					&& prevEnemyLocation[index] != null
					&& !((int)prevEnemyLocation[index].x == (int)enemyX && (int)prevEnemyLocation[index].y == (int)enemyY)
					// && enemyDistanceFromMyFlag > 40000
					)
				{
					enemyX = forkedEnemyLocation[index].x;
					enemyY = forkedEnemyLocation[index].y;
				}
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= (Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS))
				{
					m.throwBomb(i, enemyX, enemyY); 
				}
			}
			else {
				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) 
				{
					m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY));
					// walkTheDinosaur(m,i, new Point(myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)));
				}
				else 
				{
					m.setDestination(i, myX, myY); // Rest
				}
			}
		}
		else {
			// Head for the opponent's flag
			m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
			walkTheDinosaur(m,i, new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));

			// Shoot at the flag if I can hit it
			if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			}
		}

		// Try not to die
		avoidBombs(m, i);
	}

	void walkTheDinosaur(Model m, int i, Point goal) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		int goalThreshold = 5;
		//Threshold for just going directly to that destination
		if(sq_dist(myX, myY, goal.x, goal.y) <= Integer.MAX_VALUE)	
		{
			m.setDestination(i, goal.x, goal.y);
		}else
		{	
			PriorityQueue<Point> queue = new PriorityQueue<Point>();
			HashSet<Point> used = new HashSet<Point>();

			Point origin = new Point(myX, myY, 0);
			queue.add(origin);
			while(queue.size() > 0) {
				Point s = queue.remove();

				if((s.x > goal.x - goalThreshold && s.x < goal.x + goalThreshold) && (s.y > goal.y - goalThreshold && s.y < goal.y + goalThreshold) )
				{
					Point tempPoint = s;
					while(tempPoint.parent != null && tempPoint.parent.parent != null)
					{
						tempPoint = tempPoint.parent;
					}
					m.setDestination(i, (float)tempPoint.x, (float)tempPoint.y);
					break;
				}
				Point left = new Point(s.x-10, s.y, s.cost);
				Point right = new Point(s.x+10, s.y, s.cost);
				Point up = new Point(s.x, s.y-10, s.cost);
				Point down = new Point(s.x, s.y+10, s.cost );

				for(Point mystate : new Point[] { left,right,up,down})
				{

					if(!used.contains(mystate)&& mystate.x > 0 && mystate.y >0 && mystate.x < Model.XMAX && mystate.y < Model.YMAX)
					{
						//Doing magic numbers trying to get it where the fasatest tiles are the ones getting used
						float travelSpeed = (float)2.2-m.getTravelSpeed(mystate.x, mystate.y);
						int distance = (int)Math.sqrt(sq_dist(mystate.x, mystate.y, goal.x, goal.y));
						if ( travelSpeed < 0)
							travelSpeed = 0;
						mystate.cost += travelSpeed*200 ;

						mystate.parent = s;
						mystate.heuristicAndCost = (int)mystate.cost + distance;
						queue.add(mystate);
						used.add(mystate);
					}
				}	
			}
		}
	}

	public void update(Model m) {
		if(!shadow)
		{
			Controller cFork = m.getController().fork(new CoryMcDonald(true), new Mixed());
			Model mFork = cFork.getModel();
			for(int j = 0; j < 2; j++)
			{
				cFork.update();
			}
			for(int i = 0; i < mFork.getSpriteCountOpponent(); i++) 
			{
				forkedEnemyLocation[i] = new Point(mFork.getXOpponent(i), mFork.getYOpponent(i));
			}
		}
		if(forkedEnemyLocation[0] != null)
		{
			beFlagAttacker(m, 0);
			beAggressor(m, 1);
			beAlternativeDefender(m, 2);
		}

		for(int i = 0; i < m.getSpriteCountOpponent(); i++) 
		{
			prevEnemyLocation[i] = new Point(m.getXOpponent(i), m.getYOpponent(i));
		}
		iter++;
	}


	static class Point implements Comparable<Point>
	{
		Point parent;
		float x;
		float y;
		float cost;
		int heuristicAndCost; 

		public Point(float x, float y, float cost)
		{
			this.x = x;
			this.y = y;
			this.cost = cost;
		}
		public Point(float x, float y)
		{
			this.x = x;
			this.y = y;
			this.cost = (float)0;
		}
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Point)) return false;
			Point that = (Point) o;
			return ((int)x == (int)that.x && (int)y == (int)that.y);
		}
		@Override
		public int hashCode() {
			int result = (int)x;
			result = 31 * result + (int)y;
			return result;
		}
		@Override
		public int compareTo(Point state) {
			if(heuristicAndCost > state.heuristicAndCost) {
				return 1;
			}
			else if(heuristicAndCost == state.heuristicAndCost)
				return 0;

			return -1;
		}

		@Override
		public String toString() {
			return "(" + this.x + "," + this.y + ")";
		}
	}

}