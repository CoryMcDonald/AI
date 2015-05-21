import java.util.PriorityQueue;
import java.util.*;
import java.awt.*;
import java.lang.*;
import java.io.*;
// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

class Better implements IAgent
{
	int iter;
	int index; // a temporary value used to pass values around
	Random rand;
	Scanner scan;
	Better() {
		reset();
		scan = new Scanner(System.in);
		rand = new Random();
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

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
			m.setDestination(i, m.getX(i) + dx * 10.0f, m.getY(i) + dy * 10.0f);
		}
	}
	float lastDefenderX = 0;
	float lastDefenderY = 0;
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
			if( m.getX(i) <  Model.XMAX/1.73  )
			{
				float myX = m.getX(i);
				float myY = m.getY(i);
				// System.out.println("Attacking");
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;

				float destinationY = enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON);
				float destinationX = enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON);
				if(destinationX <0 || destinationX > Model.XMAX)	
					destinationX= lastDefenderX;
				if(destinationY  <0 || destinationY > Model.YMAX)	
					destinationY= lastDefenderY;
				lastDefenderX = destinationX;
				lastDefenderY = destinationY;
				// if(destinationX == Model.XMAX)
				// 	destinationX = enemyX;
				breadthFirstSearch(m,i, destinationX, destinationY);

				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i))-100 <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);

				// if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				// 	m.throwBomb(i, myX - 10.0f * (myX - enemyX+m.getTravelSpeed(enemyX, enemyY)), myY - 10.0f * (myY - enemyY+m.getTravelSpeed(enemyX, enemyY))); 
			}else
			{			
				// Stay between the enemy and my flag
				breadthFirstSearch(m,i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));
			}

			// Throw bombs if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.throwBomb(i, enemyX, enemyY);
		}
		else {
			// Guard the flag
			breadthFirstSearch(m,i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		}

		// If I don't have enough energy to throw a bomb, rest
		if(m.getEnergySelf(i) < Model.BOMB_COST)
			m.setDestination(i, m.getX(i), m.getY(i));

		// Try not to die
		avoidBombs(m, i);
	}

	float nextToRevive(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) > 0)
				continue; // consider only dead opponents
			float d = sq_dist(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}	

	int numOpponentsAlive(Model m){
		int numAlive = 0;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++)
		{
			if(m.getEnergyOpponent(i) > 0)
			{
				numAlive++;
			}
		}
		return numAlive;		
	}

	boolean isSafe(Model m, int i, float buffer){
		float myX = m.getX(i);
		float myY = m.getY(i);
		return (nearestOpponent(m, myX, myY) > (((Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS))+buffer));
	}
	void beFlagAttacker(Model m, int i) 
	{
		float myX = m.getX(i);
		float myY = m.getY(i);

			
		if(isSafe(m, i,20) && m.getEnergySelf(i) > .5)
		{
			boolean enemyCloseToRespanwn = (nextToRevive(m, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT)/Model.BROKEN_CRAWL_RATE < sq_dist(myX, myY, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT)*2 );
			if (m.getEnergySelf(i) < 1 && enemyCloseToRespanwn)
			{
				m.setDestination(i, myX, myY); // Rest
			}
			else
			{
				// Head for the opponent's flag
				breadthFirstSearch(m,i,  new Point(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT));
				// Shoot at the flag if I can hit it
				if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
					m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}
			}
		}else if(m.getEnergySelf(i) >= m.getEnergyOpponent(i)) 
		{
			float distance = nearestOpponent(m, myX, myY);

			if(index >= 0) {
				float enemyX = m.getXOpponent(index);
				float enemyY = m.getYOpponent(index);
				if((sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) && (sq_dist(enemyX, enemyY, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) < Model.BLAST_RADIUS*Model.BLAST_RADIUS)){ 
					m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}
				else
				{
					if (distance > Model.BLAST_RADIUS || m.getEnergyOpponent(index)-Model.BOMB_DAMAGE_TO_SPRITE <=0 || m.getEnergySelf(i)-(Model.BOMB_COST+Model.BOMB_DAMAGE_TO_SPRITE) > 0)
					{
						float nearestBombTarget = nearestBombTarget(m, enemyX, enemyY);

						if (nearestBombTarget > (Model.BLAST_RADIUS*Model.BLAST_RADIUS)+Model.BLAST_RADIUS+1f)
						{
							m.throwBomb(i, enemyX, enemyY);
						}
					}
				}
			}
		}
		else if(index >=0) 
		{
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			breadthFirstSearch(m,i,new Point(myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY))); // Flee
		}

		avoidBombs(m, i);
	}

	int numOfTimesStuck = 0;
	float lastAggressorX = 0;
	float lastAggressorY = 0;
	void beAggressor(Model m, int i) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)*.9) {
				// System.out.println("Trying to throw bomb at enemy");
				// Get close enough to throw a bomb at the enemy
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
				float destinationY = enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON);
				float destinationX = enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON);
				if(destinationX  <0 || destinationX > Model.XMAX)	
					destinationX= lastAggressorX;
				if(destinationY  <0 || destinationY > Model.YMAX)	
					destinationY= lastAggressorY;
				lastAggressorX = destinationX;
				lastAggressorY = destinationY;

				System.out.println(destinationX + " ," + destinationY);
				breadthFirstSearch(m,i, destinationX, destinationY);

				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i))-100 <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);

			}
			else {
				// System.out.println("Opponent is close enough to shoot me");
				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
					breadthFirstSearch(m,i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
				}
				else {
					m.setDestination(i, myX, myY); // Rest
				}
			}
		}
		else {
			// Head for the opponent's flag
			breadthFirstSearch(m,i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

			// Shoot at the flag if I can hit it
			if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			}
		}

		// Try not to die
		avoidBombs(m, i);
	}

	public void update(Model m) {
		m.getController().view.panel.clearLines();

		m.setName(2, "Flag");
		m.setName(1, "Agrees");
		m.setName(0, "Defend");

		beFlagAttacker(m, 2);
		beAggressor(m, 1);
		beAlternativeDefender(m, 0);
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


	void breadthFirstSearch(Model m, int i, float x, float y) {
		breadthFirstSearch(m,i,new Point(x,y), 3);
	}
	void breadthFirstSearch(Model m, int i, float x, float y, double threshold) {
		breadthFirstSearch(m,i,new Point(x,y), threshold);
	}

	void breadthFirstSearch(Model m, int i, Point goal) {
		breadthFirstSearch(m,i,goal, 3);
	}
	void breadthFirstSearch(Model m, int i, Point goal, double threshold) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		int goalThreshold = 10;
		int distanceToMove = 10;

		
			try
			{
				if(goal.x < 0 && goal.x > Model.XMAX) 
				{
					goal.x = myX;
				}
				if(goal.y < 0 && goal.y > Model.YMAX)
				{
					goal.y = myY;
				}
				if(m.getTravelSpeed(goal.x, goal.y) <= .5)
				{
					for(int a = (int)goal.x-60; a< goal.x+60; a++)
					{
						if(a > 1 && goal.y > 1 && a < Model.XMAX  && goal.y < Model.YMAX )
						{
							if(m.getTravelSpeed(a, goal.y) > .5)
							{
								goal.x = a;
							}
						}
					}
					for(int b = (int)goal.y-60; b< goal.y+60; b++)
					{
						if(goal.x > 1 && b > 1 && goal.x < Model.XMAX  && b < Model.YMAX )
						{
							if(m.getTravelSpeed(goal.x, b) > .5)
								goal.y = b;
						}
					}
					m.getController().view.panel.addLine((int)myX, (int)myY, (int)goal.x, (int)goal.y);
				}
				// while(m.getTravelSpeed(goal.x, goal.y) <= .5)
				// {					
				// 	if(goal.x -11 > 0  )
				// 		goal.x -= 11;
				// 	if(goal.y -11 > 0)
				// 		goal.y -= 11;
				// }
			}catch(Exception ex)
			{
				// throw ex;
			}
		

		//Threshold for just going directly to that destination
		if(sq_dist(myX, myY, goal.x, goal.y) <= 250)
		{
			m.setDestination(i, goal.x, goal.y);
		}else
		{
			PriorityQueue<Point> queue = new PriorityQueue<Point>();
			HashSet<Point> used = new HashSet<Point>();
			boolean success = false;
			Point origin = new Point(myX, myY, 0);
			Point s = null;
			int num = 0;
			queue.add(origin);
			while(queue.size() > 0) {
				s = queue.remove();
				num++;
				if((s.x > goal.x - goalThreshold && s.x < goal.x + goalThreshold) && (s.y > goal.y - goalThreshold && s.y < goal.y + goalThreshold) )
				{
					Point tempPoint = s;
					while(tempPoint.parent != null && tempPoint.parent.parent != null)
					{
						tempPoint = tempPoint.parent;
					}
					m.setDestination(i, (float)tempPoint.x, (float)tempPoint.y);
					success = true;
					break;
				}
				Point left = new Point(s.x-distanceToMove, s.y, s.cost);
				Point right = new Point(s.x+distanceToMove, s.y, s.cost);
				Point up = new Point(s.x, s.y-distanceToMove, s.cost);
				Point down = new Point(s.x, s.y+distanceToMove, s.cost );
				boolean added =false;
				for(Point mystate : new Point[] { left,right,up,down})
				{
					if(!used.contains(mystate) && mystate.x > 0 && mystate.y >0 && mystate.x < Model.XMAX && mystate.y < Model.YMAX)
					{
						//Doing magic numbers trying to get it where the fasatest tiles are the ones getting used
						float travelSpeed = (float)3.5-m.getTravelSpeed(mystate.x, mystate.y);
						int distance = (int)Math.sqrt(sq_dist(mystate.x, mystate.y, goal.x, goal.y));
						if ( travelSpeed < 0)
							travelSpeed = 0;
						mystate.cost += travelSpeed*200;
						mystate.parent = s;
						mystate.heuristicAndCost = (int)mystate.cost;
						if(travelSpeed < 3)
						{
							queue.add(mystate);
							used.add(mystate);
						}
					}
				}

			}

		}
	}
}
