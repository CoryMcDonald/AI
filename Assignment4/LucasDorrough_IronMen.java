import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;


		////////////////////////////////////
		//------------IRON MEN------------//
		////////////////////////////////////
		//CREATED BY: Lucas Dorrough	  //
		//VERSION: V0.0.1				  //
		////////////////////////////////////


//MAIN CLASS / COMMANDER
public class LucasDorrough_IronMen implements IAgent {
	int iter;
	boolean immobile;
	
	ArrayList<ally> allies;
	ArrayList<enemy> enemies;	

	LucasDorrough_IronMen() {	
		allies = new ArrayList<ally>();
		enemies = new ArrayList<enemy>();
		
		allies.add(new ally(0));
		allies.add(new ally(1));
		allies.add(new ally(2));
		
		enemies.add(new enemy(0));
		enemies.add(new enemy(1));
		enemies.add(new enemy(2));
	}
	
	public void reset() {
		iter = 0;
		immobile = false;
		
		allies = new ArrayList<ally>();
		enemies = new ArrayList<enemy>();
		
		allies.add(new ally(0));
		allies.add(new ally(1));
		allies.add(new ally(2));
		
		enemies.add(new enemy(0));
		enemies.add(new enemy(1));
		enemies.add(new enemy(2));
	}

	public void update(Model m) {			
		if (iter % 5 == 0){
			if(enemies.get(0).atRest(m) && enemies.get(1).atRest(m) && enemies.get(2).atRest(m))
				immobile = true;
		}
		
		for (int i = 0; i < m.getSpriteCountSelf(); i++) {
			//ATTACK
			if (m.getEnergyOpponent(0) < 0 && m.getEnergyOpponent(1) < 0 && m.getEnergyOpponent(2) < 0)
				allies.get(i).switchTactics(0);
			else if(immobile)	
				allies.get(i).switchTactics(0);
			
			//DEFEND
			else
				allies.get(i).switchTactics(1);
			
			
			allies.get(i).update(m);
			
			enemies.get(i).update(m);
		}
		iter++;
	}

	



//HANDLES INDIVIDUAL ALLY DECISION MAKING
class ally extends AIutils{
	int index;
	
	private int id;
	private int tactic;
	
	pathState pathStart;
	pathState pathNext;
	boolean following;
	boolean destination;
	
	int newX;
	int newY;
	
	ally(int i){
		id = i;
		tactic = 1;
		destination = false;
		following = false;
	}
		
	void update(Model m){
		if(tactic == 1)
			defend(m);
		else
			attack(m);		
	}
	
	void switchTactics(int i){
		if(tactic != i){
			tactic = i;
			following = false;
			destination = false;
		}
	}
		
	void followPath(Model m){
		if(pathNext != null && pathNext.child != null){
			if(m.getX(id) == newX && m.getY(id) == newY){
				newX = pathNext.x * 10;
				newY = pathNext.y * 10;
				m.setDestination(id, newX, newY);
				pathNext = pathNext.child;
			}
		}
		else{
			following = false;
			destination = true;
		}
	}
	
	void defend(Model m){
		//Defend the flag
		//Go to enemy flag
		if(following){
			followPath(m);
		}
		else if(!destination){
			pathStart = pathLoc(m.getX(id), m.getY(id),(100 + 200 * Math.cos((id + 16) * Math.PI / 9)), (450 + 200 * Math.sin((id + 16) * Math.PI / 9)), m);
			following = true;
			
			newX = pathStart.x * 10;
			newY = pathStart.y * 10;
			m.setDestination(id, newX, newY);
			
			pathNext = pathStart.child;
		}
	
		//attack close people
		float myX = m.getX(id);
		float myY = m.getY(id);
		nearestOpponent(m, myX, myY);
		if (index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			if (sq_dist(enemyX, enemyY, m.getX(id), m.getY(id)) <= Model.MAX_THROW_RADIUS
					* Model.MAX_THROW_RADIUS)
				m.throwBomb(id, enemyX, enemyY);
		}	
		//dodge
		dodge(m);
	}
	
	void attack(Model m){
		//if enemy is dead or not moving, travel on shortest path to flag perimeter
		//Go to enemy flag
		if(following){
			followPath(m);
		}
		else if(!destination){
			pathStart = pathFlag(m.getX(id), m.getY(id), m);
			following = true;
			
			newX = pathStart.x * 10;
			newY = pathStart.y * 10;
			m.setDestination(id, newX, newY);
			
			pathNext = pathStart.child;
		}
	
			
		//attack close people
		float myX = m.getX(id);
		float myY = m.getY(id);
		nearestOpponent(m, myX, myY);
		if (index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);
			if (sq_dist(enemyX, enemyY, m.getX(id), m.getY(id)) <= Model.MAX_THROW_RADIUS
					* Model.MAX_THROW_RADIUS)
				m.throwBomb(id, enemyX, enemyY);
		}
		
		//attack flag
		if (sq_dist(m.getX(id), m.getY(id), Model.XFLAG_OPPONENT,
				Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS
				* Model.MAX_THROW_RADIUS) {
			m.throwBomb(id, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
		}
		
		//dodge
		dodge(m);
	}
	
	void dodge(Model m){
		if (nearestBombTarget(m, m.getX(id), m.getY(id)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			following = false;
			float dx = m.getX(id) - m.getBombTargetX(index);
			float dy = m.getY(id) - m.getBombTargetY(index);
			if (dx == 0 && dy == 0)
				dx = 1.0f;
			m.setDestination(id, m.getX(id) + dx * 10.0f, m.getY(id) + dy * 10.0f);
			destination = false;
		}
		
	}
	
	
	float nearestBombTarget(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for (int i = 0; i < m.getBombCount(); i++) {
			float d = sq_dist(x, y, m.getBombTargetX(i), m.getBombTargetY(i));
			if (d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}
	

	float nearestOpponent(Model m, float x, float y) {
		index = -1;
		float dd = Float.MAX_VALUE;
		for (int i = 0; i < m.getSpriteCountOpponent(); i++) {
			if (m.getEnergyOpponent(i) < 0)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if (d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}
	
		
	
}

//KEEPS TRACK OF ENEMY MOVEMENTS
class enemy {
	private int id;
	
	private LinkedList<Float> prevX;
	private LinkedList<Float> prevY; 	
	
	enemy(int i){
		id = i;
		prevX = new LinkedList<Float>();
		prevY = new LinkedList<Float>();
	}
	
	void update(Model m){
		prevX.add(m.getXOpponent(id));
		prevY.add(m.getYOpponent(id));
		
		if(prevX.size() > 10){
			prevX.remove();
			prevY.remove();
		}
	}
	
	boolean atRest(Model m){
		if(m.getEnergyOpponent(id) <= 0)
			return true;
		
		float tempX = 0;
		float tempY = 0;
		
		float tempX2 = 0;
		float tempY2 = 0;
		
		for(int i = 0; i < prevX.size(); i++)
		{
			tempX += prevX.get(i);
			tempY += prevY.get(i);
		}
		
		tempX /= prevX.size();
		tempY /= prevY.size();
		
		for(int i = 0; i < prevX.size(); i++)
		{
			tempX2 += Math.pow(Math.abs(prevX.get(i) - tempX), 2);
			tempY2 += Math.pow(Math.abs(prevY.get(i) - tempY), 2);			
		}
		
		tempX2 /= prevX.size();
		tempY2 /= prevY.size();
		
		tempX2 = (float) Math.sqrt(tempX2);
		tempY2 = (float) Math.sqrt(tempY2);
				
		if(tempX2 < .6 && tempY2 < .6)
			return true;
		else
			return false;
	}
	
	double direction(){
		return Math.atan((prevX.get(prevX.size()-1) - prevX.get(0)) / (prevY.get(prevY.size()-1) - prevY.get(0)));
	}	
	
}

//FUNCITONS USED BY ALL
class AIutils {
	
	pathState pathFlag(float x, float y, Model m){
		double vX = (x * .1) - 110;
		double vY = (y * .1) - 15;
		double magV = Math.sqrt(vX*vX + vY*vY);
		double aX = 110 + vX / magV * 18;
		double aY = 15 + vY / magV * 18;
		
		
		pathState start = new pathState(0.0, null,(int)(x * .1),(int) (y * .1));
		pathState end = new pathState(0.0, null,(int) 100, (int) 15);
		
		return  search(start, end, m);
	}
	
	
	pathState pathLoc(float f, float g, double d, double e, Model m){
		pathState start = new pathState(0.0, null,(int) (f * .1),(int) (g * .1));
		pathState end = new pathState(0.0, null,(int) (d * .1),(int) (e * .1));
		
		return  search(start, end, m);	
	}
	
	float sq_dist(float x1, float y1, float x2, float y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}
	
	
	pathState search(pathState startState, pathState goalState,  Model m){
		PriorityQueue<pathState> frontier = new PriorityQueue<pathState>(); 
		HashMap<Integer, pathState> beenThere = new HashMap<Integer, pathState>(7200);
		startState.cost = 0.0;
		startState.parent = null;
		beenThere.put((startState.y * 120) + startState.x, startState);
		frontier.add(startState);

		while (frontier.size() > 0) {

			pathState s = frontier.poll();

			if (s.isEqual(goalState)) {
				 pathState temp = s;
				 while (temp.parent != null){
		    		  temp.parent.child = temp;
		    		  temp = temp.parent;
		    	  }
				return temp;
			}
			for (int a = 0; a < 4; a++) {
				pathState child = transition(s, a); 
				if (child.x > 119 || child.x < 0 || child.y > 59 || child.y < 0)
					continue;
				float acost = action_cost(s, a, m); 
				if (beenThere.containsKey((child.y * 120) + child.x)) {
					child = beenThere.get((child.y * 120) + child.x);
					if (s.g_cost + acost < child.g_cost) {
						child.g_cost = s.g_cost + acost;
						child.cost = child.g_cost + heuristic_cost(child, goalState);
						child.parent = s;
					}
				} else {
					child.g_cost = s.g_cost + acost;
					child.cost = child.g_cost + heuristic_cost(child, goalState);
					child.parent = s;
					frontier.add(child);
					beenThere.put((child.y * 120) + child.x, child);

				}
			}
		}
		return null;
	}

	pathState transition(pathState s, int a) {
		if (a == 0)
			return new pathState(0.0, s, s.x + 1, s.y + 0);
		if (a == 1)
			return new pathState(0.0, s, s.x + 0, s.y + 1);
		if (a == 2)
			return new pathState(0.0, s, s.x - 1, s.y + 0);
		else
			return new pathState(0.0, s, s.x + 0, s.y - 1);

	}

	float action_cost(pathState s, int a, Model m) {
		if (a == 0)
			return 1.0f / m.getTravelSpeed((s.x + 1) * 10, (s.y + 0) * 10);
		if (a == 1)
			return 1.0f / m.getTravelSpeed((s.x + 0) * 10, (s.y + 1) * 10);
		if (a == 2)
			return 1.0f / m.getTravelSpeed((s.x - 1) * 10, (s.y + 0) * 10);
		else
			return 1.0f / m.getTravelSpeed((s.x + 0) * 10, (s.y - 1) * 10);
	}

	double heuristic_cost(pathState s, pathState g) {
		return Math.sqrt((s.x - g.x) * (s.x - g.x) + (s.y - g.y) * (s.y - g.y)) * 0.3;
	}
}

class pathState implements Comparable<pathState> {
	public Double cost;
	public Double g_cost;
	pathState parent;
	pathState child;
	public int x;
	public int y;

	pathState(double cost, pathState par, int x, int y) {
		this.g_cost = 0.0;
		this.cost = cost;
		this.parent = par;
		this.x = x;
		this.y = y;
	}


	boolean isEqual(pathState m) {
		if (this.x == m.x && this.y == m.y)
			return true;
		else
			return false;
	}

	@Override
	public
	int compareTo(pathState arg0) {

		return cost.compareTo(arg0.cost);
	}
	
}

}