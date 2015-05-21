import java.util.Random;
import java.util.*;
// The contents of this file are dedicated to the public domain.
// (See http://creativecommons.org/publicdomain/zero/1.0/)

class CoryMcDonaldTurnt implements IAgent
{
	int iter, dodgeIter;
	int index, atk, def, kill; // a temporary value used to pass values around
	Random Randomizer;


	CoryMcDonaldTurnt() {
		reset();
		Randomizer = new Random();
	}

	public void reset() {
		iter = 0;
	}

	public static float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}
	
	
	static class Shadow implements IAgent
	{
		float dx;
		float dy;

		Shadow(float destX, float destY) {
			dx = destX;
			dy = destY;
		}

		public void reset() {
		}

		public void update(Model m) {
			for(int i = 0; i < 3; i++) {
				if (dx > 0 && dx < Model.XMAX && dy > 0 && dy < Model.YMAX)
					m.setDestination(i, dx, dy);
			}
		}
	}

	static class OpponentShadow implements IAgent
	{
		int index;
		OpponentShadow() {
		}

		public void reset() {
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
		
		void avoidBombs(Model m, int i) {
			if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
				float dx;
				dx = m.getX(i) + m.getBombTargetX(index);
				float dy;
				if (m.getY(i) > Model.YFLAG_OPPONENT)
					 dy = m.getY(i) - m.getBombTargetY(index);
				else
					dy = m.getY(i) + m.getBombTargetY(index);
				if(dx == 0 && dy == 0)
					dx = 1.0f;
				m.setDestination(i, m.getX(i) + dx, m.getY(i) + dy);
			}	
		}

		public void update(Model m) {
			for(int i = 0; i < m.getSpriteCountSelf(); i++) {
				nearestOpponent(m, m.getX(i), m.getY(i));

				if(index >= 0) {
					float enemyX = m.getXOpponent(index);
					float enemyY = m.getYOpponent(index);
					if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
						m.throwBomb(i, enemyX, enemyY);
				}
				avoidBombs(m, i);
			}
		}
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
	
	float bestDefender(Model m) {
		def = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			if(m.getEnergySelf(i) < 0)
				continue; // don't care about dead agents
			float d = sq_dist(Model.XFLAG, Model.YFLAG, m.getX(i), m.getY(i));
			if(d < dd) {
				dd = d;
				def = i;
			}
		}
		return dd;
	}
	
	float bestAttacker(Model m) {
		atk = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			if(m.getEnergySelf(i) < 0)
				continue; // don't care about dead agents
			float d = sq_dist(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT, m.getX(i), m.getY(i));
			if(d < dd) {
				dd = d;
				atk = i;
			}
		}
		return dd;
	}
	
	float nearestOpponentToFlag(Model m) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if(m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(Model.XFLAG, Model.YFLAG, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
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
	
	float distanceToOpponentFlag(Model m, int i){
		float d = sq_dist(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT, m.getXOpponent(i), m.getYOpponent(i));
		return d;
	}

	void avoidBombs(Model m, int i) {
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			if (i == def && Model.XFLAG > m.getX(i) && m.getEnergySelf(i) >= .5){
				float dx;
				dx = m.getX(i) + m.getBombTargetX(index);
				float dy;
				if (m.getY(i) > Model.YFLAG)
					 dy = m.getY(i) + (m.getBombTargetY(index)+5);
				else
					dy = m.getY(i) - (m.getBombTargetY(index)+5);
				if(dx == 0 && dy == 0)
					dx = 1.0f;
				m.setDestination(i, m.getX(i) + dx, m.getY(i) + dy);
			}
			else	
				forkAvoidBombs(m, i, m.getBombTargetX(index), m.getBombTargetY(index));
//			else{
//				float dx;
//				if (i == def)
//					dx = m.getX(i) - (m.getBombTargetX(index));
//				else
//					dx = m.getX(i) + m.getBombTargetX(index);
//	
//				float dy;
//				if (i == def)
//					if (m.getY(i) > Model.YFLAG)
//						 dy = m.getY(i) + (m.getBombTargetY(index)+5);
//					else
//						dy = m.getY(i) - (m.getBombTargetY(index)+5);
//				else
//					if (m.getY(i) > Model.YFLAG_OPPONENT)
//						 dy = m.getY(i) - m.getBombTargetY(index);
//					else
//						dy = m.getY(i) + m.getBombTargetY(index);
//	
//				if(dx == 0 && dy == 0)
//					dx = 1.0f;
//				m.setDestination(i, m.getX(i) + dx, m.getY(i) + dy);
//			}	
			dodgeIter++;
		}
	}
	
	boolean isSafe(Model m, int i, float buffer){
		float myX = m.getX(i);
		float myY = m.getY(i);
		return (nearestOpponent(m, myX, myY) > (((Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS))+buffer));
	}
	
	int numOpponentsInRange(Model m, int i, float buffer){
		float myX = m.getX(i);
		float myY = m.getY(i);
		int numInRange = 0;
		for(int j = 0; j < m.getSpriteCountOpponent(); j++)
			if(m.getEnergyOpponent(j) > 0 && sq_dist(myX, myY, m.getXOpponent(j), m.getYOpponent(j)) <= (((Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)))+buffer)
				numInRange++;
		return numInRange;		
	}
	
	int numOpponentsAlive(Model m){
		int numAlive = 0;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++)
			if(m.getEnergyOpponent(i) > 0)
				numAlive++;
		return numAlive;		
	}
	
	boolean revivalImminent(Model m, float offset){
		for(int i = 0; i < m.getSpriteCountOpponent(); i++) 
			if(m.getEnergyOpponent(i) <= 0)
				if (sq_dist(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT, m.getXOpponent(i), m.getYOpponent(i)) < offset*offset)
					return true;
		return false;
	}
	
	
	void forkAvoidBombs(Model m, int i, float bombX, float bombY) {
		dodgeIter++;

		float bestX = m.getX(i);
		float bestY = m.getY(i);
//		float best_iEnergy = Model.XMAX*Model.XMAX;
		float bestDodge = 0;
		for(int sim = 0; sim < 8; sim++) { // try 8 candidate destinations
			float x = (float)(m.getX(i)+(Math.cos((Math.PI*sim)/4)*Model.BLAST_RADIUS));
			float y = (float)(m.getY(i)+(Math.sin((Math.PI*sim)/4)*Model.BLAST_RADIUS));

			// Fork the universe and simulate it for 10 time-steps
			Controller cFork = m.getController().fork(new Shadow(x, y), new OpponentShadow());
			Model mFork = cFork.getModel();
			for(int j = 0; j < 10; j++)
				cFork.update();

			// See how close the current sprite got to the opponent's flag in the forked universe
//			float sqd = sq_dist(mFork.getX(i), mFork.getY(i), XGoal, YGoal);
//			float iEnergy = sq_dist(mFork.getX(i), mFork.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			float dodge = sq_dist(mFork.getX(i), mFork.getY(i), bombX, bombY);

			if(dodge > bestDodge) {
				bestDodge = dodge;
				bestX = x;
				bestY = y;
			}
		}

		// Head for the point that worked out best in simulation
		m.setDestination(i, bestX, bestY);
	}
	

	void beDefender(Model m, int i) {
		// Find the opponent nearest to my flag
		nearestOpponentToFlag(m);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			// Throw bombs if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.throwBomb(i, enemyX, enemyY);
			
			// Stay between the enemy and my flag
			if(sq_dist(enemyX, enemyY, Model.XFLAG, Model.YFLAG) > Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));
			else
				m.setDestination(i, 0.75f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

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
		// Head for the opponent's flag

		m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

		float myX = m.getX(i);
		float myY = m.getY(i);
		
		if (isSafe(m, i, 20)){
			if (m.getEnergySelf(i) < 1 && numOpponentsAlive(m) > 0 && nextToRevive(m, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT)/Model.BROKEN_CRAWL_RATE > sq_dist(myX, myY, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT)*.5)
				m.setDestination(i, myX, myY); // Rest
			else{
//				if(iter % 10 == 0)
					breadthFirstSearch(m, i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
//				else
				// Avoid opponents
				nearestOpponent(m, myX, myY);
				
				if(index >= 0) {
					float enemyX = m.getXOpponent(index);
					float enemyY = m.getYOpponent(index);
					if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS*1.5) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS*1.5))
//						if (iter%1==0)
							breadthFirstSearch(m, i, myX + 15.0f * (myX - enemyX), myY + 15.0f * (myY - enemyY));
//						else
//							m.setDestination(i, myX + 15.0f * (myX - enemyX), myY + 15.0f * (myY - enemyY));
				}
				
				if (numOpponentsInRange(m, i, 0) == 0 && !revivalImminent(m, 100)){
//					if (iter%20==0)
//						breadthFirstSearch(m, i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
//					else
						m.setDestination(i, Model.XFLAG_OPPONENT - 100, Model.YFLAG_OPPONENT);
				}
				if (revivalImminent(m, 100)){
					m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS +1, Model.YFLAG_OPPONENT);
					float d = nextToRevive(m, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
					if (index >= 0){
						if (d <= 100){
							float enemyX = m.getXOpponent(index);
							float enemyY = m.getYOpponent(index);
							if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
								m.throwBomb(i, enemyX, enemyY);
						}
						else
							if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) 
								m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
					}
				}
				else{		
					if (iter%35==0)
						breadthFirstSearch(m, i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
					else
						m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

					// Shoot at the flag if I can hit it
					if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) 
						m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}	
			}
		}
		else{ //it is not safe!
			float distance = nearestOpponent(m, myX, myY);

			if(index >= 0) {
				float enemyX = m.getXOpponent(index);
				float enemyY = m.getYOpponent(index);

				// If the opponent is close enough to shoot at me...
//				if ((sq_dist(myX, myY, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) < sq_dist(myX, myY, enemyX, enemyY)) && ((myX>Model.XFLAG_OPPONENT && enemyX<Model.XFLAG_OPPONENT)||(myX<Model.XFLAG_OPPONENT && enemyX>Model.XFLAG_OPPONENT))){
////					m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
				if((sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) && (sq_dist(enemyX, enemyY, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) < Model.BLAST_RADIUS*Model.BLAST_RADIUS)){ 
						m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
				}
				else{
					if (distance > Model.BLAST_RADIUS || m.getEnergyOpponent(index)-Model.BOMB_DAMAGE_TO_SPRITE <=0 || m.getEnergySelf(i)-(Model.BOMB_COST+Model.BOMB_DAMAGE_TO_SPRITE) > 0)
						if (nearestBombTarget(m, enemyX, enemyY) > (Model.BLAST_RADIUS*Model.BLAST_RADIUS)+Model.BLAST_RADIUS+1f)
							m.throwBomb(i, enemyX, enemyY);
//					m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
				}
			}
		}

//		// Shoot at the flag if I can hit it
//		if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
//			m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
//		}
//		else{
//			nearestOpponent(m, myX, myY);
//			if(index >= 0) {
//				float enemyX = m.getXOpponent(index);
//				float enemyY = m.getYOpponent(index);
//
//				if(m.getEnergySelf(i) >= m.getEnergyOpponent(index)) {
//
//					// Get close enough to throw a bomb at the enemy
//					float dx = myX - enemyX;
//					float dy = myY - enemyY;
//					float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
//					dx *= t;
//					dy *= t;
//					m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
//
//					// Throw bombs
//					if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
//						m.throwBomb(i, enemyX, enemyY);
//				}
//				else{
//					m.setDestination(i, Model.XFLAG, Model.YFLAG);
//					atk = def;
//					def = i;
//				}
//
//			}
//		}
			

		// Try not to die
		
		nearestOpponent(m, myX, myY);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			if (!(m.getEnergySelf(i)>=.2 && m.getEnergySelf(i)-(Model.BOMB_DAMAGE_TO_SPRITE+Model.BOMB_COST) > m.getEnergyOpponent(index)))
				avoidBombs(m, i);

		}
	}

	void beAggressor(Model m, int i) {
		float myX = m.getX(i);
		float myY = m.getY(i);

		// Find the opponent nearest to me
//		nearestOpponent(m, myX, myY);
		nearestOpponentToFlag(m);

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
				if (iter%5==0)
					breadthFirstSearch(m, i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
				else
					m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));

				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
					m.throwBomb(i, enemyX, enemyY);
			}
			else {

				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) + 5.0f) {
//					if (iter%5==0)
						breadthFirstSearch(m, i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY));
//					else
//						m.setDestination(i, myX + 5.0f * (myX - enemyX), myY + 5.0f * (myY - enemyY)); // Flee
				}
				else {
						m.setDestination(i, myX, myY); // Rest
				}
			}
		}
		else {
			// Head for the opponent's flag
			m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);

			// Shoot at the flag if I can hit it
			if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS) {
				m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
			}
		}

		// Try not to die
		avoidBombs(m, i);
	}

	void chooseRole(Model m, int i){
		if (iter%30 == 0){
			bestAttacker(m);
			bestDefender(m);
		}
		while (true){
			if (((numOpponentsAlive(m) <= 1 && nextToRevive(m, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) >= 100*100) || (numOpponentsAlive(m) == 0)) && nearestOpponentToFlag(m) >= 500*500){
//				if (index >= 0){
//					if (def == i)
//						if (m.getEnergySelf(i) - Model.BOMB_DAMAGE_TO_SPRITE <=0 && m.getEnergyOpponent(index) - Model.BOMB_DAMAGE_TO_SPRITE > m.getEnergySelf(i))
//							beDefender(m, i);
//						else
//							beAggressor(m, i);
//					else
//						beFlagAttacker(m, i);
//				}
//				else
					beFlagAttacker(m, i);
				break;
			}
			else{
				// if (def == i){
				// 	beDefender(m, i);
				// 	break;
				// }
				if (atk == i){
					if (numOpponentsInRange(m, def, 250) <= 1 || m.getEnergySelf(def)>.75)
						beFlagAttacker(m, i);
					else
						beAggressor(m, i);
					break;
				}
				if (m.getFlagEnergyOpponent() < .5 && numOpponentsInRange(m, def, 250) == 0)
					beFlagAttacker(m, i);
				else
					beAggressor(m, i);
				break;	
			}
				
		}
	}
	
	
	public void update(Model m) {
		for(int i = 0; i < m.getSpriteCountSelf(); i++) {
			if(m.getEnergySelf(i) > 0)
				chooseRole(m, i);
		}
//		beFlagAttacker(m, 0);
//		beAggressor(m, 1);
//		beDefender(m, 2);

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


