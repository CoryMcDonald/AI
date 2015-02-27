// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)
import java.util.PriorityQueue;
import java.util.*;
import java.awt.*;
class CoryMcDonald implements IAgent
{
	int iter;
	int index; // a temporary value used to pass values around

	int prevEnemyX;
	int prevEnemyY;

	CoryMcDonald() {
		reset();
	}

	public void reset() {
		iter = 0;
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
			if(m.getEnergyOpponent(i) < 0)
				numOfDead++;
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

	void beDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			// Stay between the enemy and my flag
			m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

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

	void beAlternativeDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			//Oh snap enemy is on my side of the board imma attack em
			if(enemyX < Model.XMAX/1.5 )
			{
				// System.out.println("I'm in your area");
				
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
				// System.out.println("Defending");
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
			float enemyDistance = sq_dist(enemyX, enemyY, myX, myY);
			float distanceFromFlag = sq_dist(myX, myY, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
			//If an enemy is closer to me than 3x from the flag I am under attack I should defend myself!
			if(enemyDistance*3 < distanceFromFlag && m.getEnergySelf(i) >= m.getEnergyOpponent(index) 
				|| (numberOfDeadOpponents(m) > 0 && m.getEnergySelf(i) >= m.getEnergyOpponent(index)) && enemyDistance*.1 < distanceFromFlag )			
			{
				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			
			}
			else
			{
				// System.out.println(m.getEnergySelf(i) + " < " +  m.getEnergyOpponent(index));
				if ( m.getEnergySelf(i) < m.getEnergyOpponent(index)) 
				{
					m.setDestination(i, myX, myY); // Rest
				}else
				{
					walkTheDinosaur(m,i, new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));
					// System.out.println("Flag distance: " + sq_dist(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1,  Model.YFLAG_OPPONENT, myX, myY));
					if(enemyDistance <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS))
					{
						// System.out.println("Flee!!!");
						m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY));
					}
				}

			}
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
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;

				// m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
				walkTheDinosaur(m,i, new Point(enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON),(float)0));
				// Throw bombs, enemy is close enough to me
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				{
					
					//If the enemy is cornered
					// if( myX > Model.XMAX-200 && myY < 150)
					// {
					// 	m.throwBomb(i, enemyX-40,enemyY); 
					// }else
					// {
						// This should anticipate where the enemy is going to move and fire at that spot
						m.throwBomb(i, myX - 10f * (myX - enemyX+m.getTravelSpeed(enemyX, enemyY)), myY - 10f * (myY - enemyY+m.getTravelSpeed(enemyX, enemyY))); 
					// }
				}
			}
			else {
				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
					m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
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
		// System.out.println(sq_dist(myX, myY, goal.x, goal.y));
		if(sq_dist(myX, myY, goal.x, goal.y) <= 2500)
		{
			m.setDestination(i, goal.x, goal.y);
		}else
		{	

		// if(myX )
			PriorityQueue<Point> queue = new PriorityQueue<Point>();
			HashSet<Point> used = new HashSet<Point>();

			Point origin = new Point(myX, myY, 0);
			queue.add(origin);
			while(queue.size() > 0) {
				Point s = queue.remove();

				if((s.x > goal.x - goalThreshold && s.x < goal.x + goalThreshold) && (s.y > goal.y - goalThreshold && s.y < goal.y + goalThreshold) )
				{
					Point tempPoint = s;
				// System.out.println(tempPoint);
					while(tempPoint.parent != null && tempPoint.parent.parent != null)
					{
						tempPoint = tempPoint.parent;
					}
				// System.out.println(tempPoint);
				// int distance = (int)Math.sqrt(sq_dist(tempPoint.x, tempPoint.y, goal.x, goal.y));
				// System.out.println(distance);

					m.setDestination(i, (float)tempPoint.x, (float)tempPoint.y);
				//set destination to the top parent
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
						float travelSpeed = (float)2.2-m.getTravelSpeed(mystate.x, mystate.y);
						int distance = (int)Math.sqrt(sq_dist(mystate.x, mystate.y, goal.x, goal.y));
						if ( travelSpeed < 0)
							travelSpeed = 0;
						mystate.cost += travelSpeed*2000 + distance;
					// System.out.println(mystate + " - " + mystate.cost);
						mystate.parent = s;
						mystate.heuristicAndCost = (int)mystate.cost;
						queue.add(mystate);
						used.add(mystate);
					}
				}	
			}
		}
	}
	public void update(Model m) {
		// beFlagAttacker(m, 0);
		// beFlagAttacker(m, 2);
		// beFlagAttacker(m, 1);
		beFlagAttacker(m, 0);
		beAggressor(m, 1);
		beAlternativeDefender(m, 2);


		iter++;
	}
	private int heuristic(Point origin, Point goal)
	{
		return 1 * Math.abs((int)goal.x-(int)origin.x) + Math.abs((int)goal.y-(int)origin.y);
	}
}
class Point implements Comparable<Point>
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

        // System.out.println(x + " == " + that.x + " && " + y + " == " + that.y);
		return ((int)x == (int)that.x && (int)y == (int)that.y);

        // if (x != that.x) return false;
        // if (y != that.y) return false;

        // return false;
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
