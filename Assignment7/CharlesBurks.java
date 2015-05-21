/*
 * Created By Charles Luke Burks for AI Homework 4
 * 
 * 
 * 
 */




import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.TreeSet;



public class CharlesBurks implements IAgent{

	int iter; 
	int index; 
	ArrayList<Stack<point>> paths; 
	
	
	CharlesBurks() throws IOException{
		reset();
	}
	
	public void reset(){
		iter = 0; 
		paths = new ArrayList<Stack<point>>(); 
		for(int i = 0; i<7; i++)paths.add(new Stack<point>());
		
		
	}
	

	
	public static float sq_dist(float x1, float y1, float x2, float y2){
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}

	float nearestBombTarget(Model m, float x, float y){
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

	float nearestOpponent(Model m, float x, float y, boolean includeDead){
		index = -1;
		float dd = Float.MAX_VALUE;
		for(int i = 0; i < m.getSpriteCountOpponent(); i++){
			if(m.getEnergyOpponent(i) < 0 && includeDead == false)
				continue; // don't care about dead opponents
			float d = sq_dist(x, y, m.getXOpponent(i), m.getYOpponent(i));
			if(d < dd) {
				dd = d;
				index = i;
			}
		}
		return dd;
	}
	
	
	
	void avoidBombs(Model m, int i){
		if(nearestBombTarget(m, m.getX(i), m.getY(i)) <= 2.0f * Model.BLAST_RADIUS * Model.BLAST_RADIUS) {
			float dx = m.getX(i) - m.getBombTargetX(index);
			float dy = m.getY(i) - m.getBombTargetY(index);
			if(dx == 0 && dy == 0)
				dx = 1.0f;
				
			if(m.getTravelSpeed(m.getX(i), m.getY(i)) > .915f){
				m.setDestination(i, (m.getX(i) + dx * 27.0f), (m.getY(i) + dy * 27.0f));
			}	
		}
		 
	}
	
	void beDefender(Model m, int i){
		// Find the opponent nearest to my flag
		nearestOpponent(m, Model.XFLAG, Model.YFLAG,false);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			// Stay between the enemy and my flag
			m.setDestination(i, 0.5f * (Model.XFLAG + enemyX), 0.5f * (Model.YFLAG + enemyY));

			// Throw bombs if the enemy gets close enough
			if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS)
				m.throwBomb(i, enemyX, enemyY);
		}
		else{
			// Guard the flag
			m.setDestination(i, Model.XFLAG + Model.MAX_THROW_RADIUS, Model.YFLAG);
		}

		// If I don't have enough energy to throw a bomb and take a hit and then some, rest
		if(m.getEnergySelf(i)-.7 < Model.BOMB_COST)
			m.setDestination(i, m.getX(i), m.getY(i));

		// Try not to die
		avoidBombs(m, i);
	}






	@SuppressWarnings("static-access")
	void beCharger(Model m, int i,boolean goForY){
			
		float myX = m.getX(i);
		float myY = m.getY(i);
		
			//use astar to find fastest path to flag
			//store path in p* stacks
		
		
		if(paths.get(i) == null || paths.get(i).size() == 0){
			State start = new State(0,null,(int)m.getX(i),(int)m.getY(i)); 	
			State goal = new State(0,null,(int)(Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1),(int)m.YFLAG_OPPONENT); 
			if(goForY)goal = new State(0,null,(int)(Model.XFLAG_OPPONENT),(int)(m.YFLAG_OPPONENT+ Model.MAX_THROW_RADIUS - 1));
			State s = AStarSearch(start,goal,true,m); 
			paths.set(i,new Stack<point>()); 
			while(s.parent != null){
				paths.get(i).push(new point(s.x,s.y)); 
				s = s.parent; 
			}
		}
		
			
			//rest up
			if((m.getEnergySelf(i) < 1 && myX < m.XFLAG + 200)){
				m.setDestination(i, myX, myY); // Rest
			}
			else{
				
				
					int distMin = 10; 
					
					if(paths.get(i).size() != 0){
						point p = paths.get(i).peek(); 
						if(Math.abs(p.x-m.getX(i)) < distMin && Math.abs(p.y-m.getY(i)) < distMin){
							p = paths.get(i).pop(); 
						}
						m.setDestination(i, (float)p.x,(float)p.y);
					}
					else{
						// Head for the opponent's flag
						
						if(Math.sqrt(sq_dist(myX,myY,Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1,Model.YFLAG_OPPONENT)) < 15){
							m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
						}
						else if(Math.sqrt(sq_dist(myX,myY,Model.XFLAG_OPPONENT,Model.YFLAG_OPPONENT + Model.MAX_THROW_RADIUS -1)) < 15){
							m.setDestination(i,Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT+Model.MAX_THROW_RADIUS-1);
						}
						else{
							if(!goForY){
								m.setDestination(i, Model.XFLAG_OPPONENT - Model.MAX_THROW_RADIUS + 1, Model.YFLAG_OPPONENT);
							}
							else m.setDestination(i,Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT+Model.MAX_THROW_RADIUS-1);
						}
					}
					
					
				}
				
				
				nearestOpponent(m, myX, myY,false);
				if(index >= 0){
					float enemyX = m.getXOpponent(index);
					float enemyY = m.getYOpponent(index);
					// Throw bombs
					if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS && m.getTravelSpeed(m.getXOpponent(i), m.getYOpponent(i)) < 1.2)
						m.throwBomb(i, enemyX, enemyY);
				}
				
				if(m.getEnergySelf(i) > .3){
					// Shoot at the flag if I can hit it
					if(sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS && sq_dist(m.getX(i), m.getY(i), Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT) > Model.BLAST_RADIUS*Model.BLAST_RADIUS){
						m.throwBomb(i, Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
					}
				}
				
			
			
			// Try not to die
			avoidBombs(m, i);
				
		}

	
	@SuppressWarnings("static-access")
	void beAssassin(Model m, int i){
		float myX = m.getX(i);
		float myY = m.getY(i);

		// Find the opponent nearest to me
		nearestOpponent(m, myX, myY,false);
		if(index >= 0) {
			float enemyX = m.getXOpponent(index);
			float enemyY = m.getYOpponent(index);

			if((m.getEnergySelf(i)-.3 >= m.getEnergyOpponent(index)) || m.getEnergySelf(i) > .8){

				
				float dx = myX - enemyX;
				float dy = myY - enemyY;
				float t = 1.0f / Math.max(Model.EPSILON, (float)Math.sqrt(dx * dx + dy * dy));
				dx *= t;
				dy *= t;
			
				
				
			
					if(paths.get(i+3) == null || paths.get(i+3).size() == 0){
						State start = new State(0,null,(int)m.getX(i),(int)m.getY(i)); 
						State goal = new State(0,null,((int)(enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON))), (int)(enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON))); 
						State s = AStarSearch(start,goal,true,m); 
						paths.set(i+3,new Stack<point>()); 
						while(s.parent != null){
							paths.get(i+3).push(new point(s.x,s.y)); 
							s = s.parent; 
						}
					}
			
				
				//rest up
				if((m.getEnergySelf(i) < 1 && myX < m.XFLAG + 200)){
					m.setDestination(i, myX, myY); // Rest
				}
				else{
				
					int distMin = 20; 
					
					
					
					if(paths.get(i+3).size()!=0){
						point p = paths.get(i+3).peek(); 
						if(Math.abs(p.x-m.getX(i)) < distMin && Math.abs(p.y-m.getY(i)) < distMin){
							p = paths.get(i+3).pop(); 
						}
						m.setDestination(i, (float)p.x,(float)p.y);
					}
					else{
					m.setDestination(i, enemyX + dx * (Model.MAX_THROW_RADIUS - Model.EPSILON), enemyY + dy * (Model.MAX_THROW_RADIUS - Model.EPSILON));
					}
				
				
				
				}
				
				
				// Throw bombs
				if(sq_dist(enemyX, enemyY, m.getX(i), m.getY(i)) <= Model.MAX_THROW_RADIUS * Model.MAX_THROW_RADIUS){
					m.throwBomb(i, enemyX, enemyY);
				}
					
				
				
				
			}
			else {

				// If the opponent is close enough to shoot at me...
				if(sq_dist(enemyX, enemyY, myX, myY) <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS)) {
					m.setDestination(i, myX + 10.0f * (myX - enemyX), myY + 10.0f * (myY - enemyY)); // Flee
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
	


	@SuppressWarnings("static-access")
	public void update(Model m){
		
		
		
		
		long saveTime = System.nanoTime(); 
			
		
		
		
		boolean isOneDead = false;  
		int farthestFromHome = 0; 
		int closestToHome = 1; 
		int middle = 2; 
		boolean dangerClose = false; 
		
		for(int i = 0; i<3; i++){
			if(m.getEnergyOpponent(i) <= .1f){
				isOneDead = true;
			}
			if(Math.sqrt(sq_dist(m.getXOpponent(i),m.getYOpponent(i),m.XFLAG,m.YFLAG)) < 280 && m.getEnergyOpponent(i)>.05f)dangerClose = true; 
		}
		
		float sd0 = sq_dist(m.getX(0),m.getY(0),m.XFLAG,m.YFLAG);
		float sd1 = sq_dist(m.getX(1),m.getY(1),m.XFLAG,m.YFLAG);
		float sd2 = sq_dist(m.getX(2),m.getY(2),m.XFLAG,m.YFLAG);
		
		if(m.getEnergySelf(0) <  .1)sd0*=(sd0*sd0); 
		if(m.getEnergySelf(1) <  .1)sd1*=(sd1*sd1); 
		if(m.getEnergySelf(2) <  .1)sd2*=(sd2*sd2); 
		
		if(sd0 > Math.max(sd1, sd2)){
			farthestFromHome = 0; 
			
			if(sd1 > sd2){
				closestToHome = 2; 
				middle = 1;
			}
			else{
				closestToHome = 1; 
				middle = 2; 
			}
		}
		else if(sd1 > Math.max(sd0,sd2)){
			farthestFromHome = 1; 
			
			if(sd0 > sd2){
				closestToHome = 2; 
				middle = 0;
			}
			else{
				closestToHome = 0; 
				middle = 2; 
			}
		}
		else if(sd2 > Math.max(sd1,sd0)){
			farthestFromHome = 2; 
			
			if(sd1 > sd0){
				closestToHome = 0; 
				middle = 1;
			}
			else{
				closestToHome = 1; 
				middle = 0; 
			}
		}
		
		
		
		if(isOneDead && !dangerClose){
			beDefender(m,closestToHome); 
			beCharger(m,farthestFromHome,true); 
			beCharger(m,middle,false); 
		}
		else if(dangerClose){
			beAssassin(m,farthestFromHome); 
			beDefender(m,middle);
			beDefender(m,closestToHome); 			

		}
		else{
			beAssassin(m,farthestFromHome); 
			beAssassin(m,middle); 
			beDefender(m,closestToHome); 

		}
		
		
		

		//System.out.println(System.nanoTime()-saveTime + "  " + m.getTimeBalance()); 
		
		iter++;
		
	}
	
	
	
	
	//below are classes implementing state, point, and astar search/universal cost search 
	
	
	
	
	@SuppressWarnings("rawtypes")
	static class State implements Comparator{
		  public double cost;
		  State parent;
		  public int x;
		  public int y;
		  int numPops; 
		  public double tCost; 
		  public double costSoFar; 
		  
		  State(){
			  
		  }
		  
		  State(State a){
			  this.cost = a.cost; 
			  this.parent = a.parent; 
			  this.x = a.x; 
			  this.y = a.y; 
		  }
		  
		  State(double cost, State par, int x, int y){
			  this.cost = cost; 
			  this.parent = par; 
			  this.x = x; 
			  this.y = y; 
			  
		  }
		  
		 
		  
		  boolean isEqual(State b){
			  if(this.x == b.x && this.y == b.y)return true; 
			  else return false; 
		  }

		  @Override
			public int compare(Object arg0, Object arg1) {
				State a = (State)arg0; 
				State b = (State)arg1; 
			
				
					if(a.cost > b.cost)return 1; 
					else if(a.cost == b.cost)return 0; 
					else return -1; 
				
				

		  }
		  
		}
	
	
	@SuppressWarnings("rawtypes")
	static class point implements Comparator{
		
		int x; 
		int y; 
		
		point(){
			
		}
		
		point(int X, int Y){
			this.x = X; 
			this.y = Y; 
		}

		@Override
		public int compare(Object arg0, Object arg1) {

			point a = (point)arg0; 
			point b = (point)arg1; 
			
			if(a.x == b.x && a.y == b.y)return 0; 
			else if(a.x > b.x)return 1; 
			else if(a.x < b.x)return -1; 
			else if(a.y > b.y)return 1; 
			else if(a.y < b.y)return -1; 
			
			return 0;
		}
			
		
	}
	
	
	
	@SuppressWarnings("unchecked")
	State AStarSearch(State startState, State goalState, boolean heuristic, Model m){
		  
		  
		  Comparator<State> comp = new State(); 
		  PriorityQueue<State> frontier = new PriorityQueue<State>(1000000,comp); // lowest cost comes out first
		  startState.costSoFar = 0.0;
		  if(heuristic == true){
			  startState.cost = heuristic(startState,goalState); 
		  }
		  else startState.cost = 0.0; 
		  startState.parent = null;
		  
		  frontier.add(startState);

		  
		  
		 
	    
		  Comparator<point> comp2 = new point(); 
		  TreeSet<point> beenThere = new TreeSet<point>(comp2);  //better way of checking
		  beenThere.add(new point(startState.x,startState.y)); 
		  
		  
		  int count = 0; 
		  while(frontier.size() > 0) {

			  count++; 
			  
			  	//pull first state from the queue
			  	State s = (State) frontier.poll(); 
 	
			  	if(s.isEqual(goalState)){
			  		s.numPops = count; 
			  		return s;
			  	}
  
			  	// load all allowable states into an array
	    	  	ArrayList<State> children = getChildrenStates(s); 
	    	  	
	    	  
	    	  	//check each allowable state
	    	 	for(int i = 0; i<children.size(); i++){
	    		  
	    	 		State child = children.get(i); 

	    	 		//find cost
	    	 		double acost = 100/(m.getTravelSpeed((float)child.x,(float)child.y)*m.getTravelSpeed((float)child.x,(float)child.y));//*m.getTravelSpeed((float)child.x,(float)child.y); 
	    	 		
	    	 		
	    	 		
	 

	    	 		if(beenThere.contains(new point(child.x,child.y))){
	    	 			continue; 
	    	 		}
	    	 		else{	
	    	 			child.costSoFar = s.costSoFar + acost;
	    	 			child.parent = s;
	    	 			if(heuristic == true){
	    	 				child.cost = child.costSoFar+heuristic(child,goalState);
	    	 			}
	    	 			else child.cost = child.costSoFar; 
	    	 			
	    	 			beenThere.add(new point(child.x,child.y));
	    	 			frontier.add(child);
	    	 		} 		
	    	 	}	
		  }
		  
		  
		  return startState; 

	  }
	
	
	  double heuristic(State now,State goal){
		  return Math.abs(now.x-goal.x)+Math.abs(now.y-goal.y);
	  }
	
	  ArrayList<State> getChildrenStates(State s){	  
		  
		  int stepSize = 10; 
		  	ArrayList<State> a = new ArrayList<State>(); 
			if(s.x < 1200-stepSize)a.add(new State(0,null,s.x+stepSize,s.y)); 
		  	if(s.x > 0+stepSize)a.add(new State(0,null,s.x-stepSize,s.y));
		  	if(s.y < 600-stepSize)a.add(new State(0,null,s.x,s.y+stepSize));
		  	if(s.y > 0+stepSize)a.add(new State(0,null,s.x,s.y-stepSize));

		  	
		  	return a; 
	  }
	  
	  
}








