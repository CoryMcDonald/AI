import java.util.ArrayList;
import java.util.Random;
class Game {

	private static CoryMcDonaldGenetic bestAround;

	static void fullTournament() throws Exception {
		ArrayList<IAgent> al = new ArrayList<IAgent>();
		al.add(new CoryMcDonald());
		// al.add(new CoryMcDonaldGenetic());
		// al.add(new CoryMcDonaldGenetic2());
		// al.add(new LucasDorrough_IronMen());
		al.add(new PrescientMoron());
		al.add(new Mixed2());
		al.add(new Mixed());
		al.add(new Human());
		al.add(new Blitz());
		al.add(new SittingDuck());
		al.add(new AggressivePack());
		Controller.doTournament(al);
	}

	public static void main(String[] args) throws Exception {

		Random rand = new Random();
		ArrayList<IAgent> population = new ArrayList<IAgent>();
		population.add(new CoryMcDonaldGenetic(2.9681700016639985,0.9875496984633174,0.08285741182279571,0.9479174240537996, true));

		for(int i=0; i<100; i++)
		{
			// System.out.println(i);
			double randomNum = Math.random();
			if(randomNum < .25 && population.size() > 0)
			{
				System.out.println(i + " mutating");
				int randGuy =  rand.nextInt(population.size());
				double rannum = Math.random();
				if(rannum < .2)
					((CoryMcDonaldGenetic)population.get(randGuy)).geneticDefenderTravel = 0.001 + (4 - 0.001 ) * rand.nextDouble();
				else if (rannum < .45)
					((CoryMcDonaldGenetic)population.get(randGuy)).whenAttackEnemyAsFlag = rand.nextDouble();
				else if(rannum < .65)
					((CoryMcDonaldGenetic)population.get(randGuy)).whenFleeAsFlag  = rand.nextDouble();
				else if(rannum < .9)
					((CoryMcDonaldGenetic)population.get(randGuy)).whenToDirectlyAttackAsAggressor = rand.nextDouble();
				else if(rannum < 1)
				{
					if(Math.random() > .5)
						((CoryMcDonaldGenetic)population.get(randGuy)).predictEnemyMovement = true;
					else
						((CoryMcDonaldGenetic)population.get(randGuy)).predictEnemyMovement = false;
				}
			}
			else if (randomNum < .5 && population.size() < 20)
			{
				System.out.println(i + " adding");

				double geneticDefenderTravel = 0.001 + (4 - 0.001 ) * rand.nextDouble();
				double whenAttackEnemyAsFlag = rand.nextDouble();
				double whenFleeAsFlag  = rand.nextDouble();
				double whenToDirectlyAttackAsAggressor = rand.nextDouble();
				boolean predict = true;
				if(Math.random() > .5)
				{
					predict = false;
				}

				population.add(new CoryMcDonaldGenetic(geneticDefenderTravel,whenAttackEnemyAsFlag,whenFleeAsFlag,whenToDirectlyAttackAsAggressor,predict));
			}else if(randomNum < .6 && population.size() > 0)
			{
				System.out.println(i + " removing");

				population.remove(rand.nextInt(population.size()));
			}else if (population.size() > 1)
			{
				System.out.println(i + " tournament");

				int[] wins = new int[population.size()];
				int[] winners = Controller.rankAgents(population, wins, false);

				int winnerIndex = winners[0];
				CoryMcDonaldGenetic winnerParent = (CoryMcDonaldGenetic)population.get(winnerIndex);

				population.remove(winners[winners.length-1]);


				double geneticDefenderTravel = winnerParent.geneticDefenderTravel ;
				double whenAttackEnemyAsFlag = winnerParent.whenAttackEnemyAsFlag ;
				double whenFleeAsFlag  = winnerParent.whenFleeAsFlag  ;
				double whenToDirectlyAttackAsAggressor = winnerParent.whenToDirectlyAttackAsAggressor ;
				boolean predictEnemyMovement = winnerParent.predictEnemyMovement ;

				CoryMcDonaldGenetic randomMom = (CoryMcDonaldGenetic)population.get(rand.nextInt(population.size()));
				if(Math.random() < .15 ) {
					geneticDefenderTravel = randomMom.geneticDefenderTravel;
				}
				if(Math.random() < .3) {
					whenAttackEnemyAsFlag = randomMom.whenAttackEnemyAsFlag;
				}
				if(Math.random() > .9) {
					whenFleeAsFlag = randomMom.whenFleeAsFlag;
				}
				if(Math.random() > .75) {
					whenToDirectlyAttackAsAggressor = randomMom.whenToDirectlyAttackAsAggressor;
				}
				if(Math.random() > .5)
					predictEnemyMovement = randomMom.predictEnemyMovement;

				population.add(new CoryMcDonaldGenetic(geneticDefenderTravel,whenAttackEnemyAsFlag,
					whenFleeAsFlag,whenToDirectlyAttackAsAggressor,predictEnemyMovement));
			}
		}
		int[] wins = new int[population.size()];
		int[] winners = Controller.rankAgents(population, wins, false);

		int winnerIndex = winners[0];
		bestAround = (CoryMcDonaldGenetic)population.get(winnerIndex);
		System.out.println(bestAround.geneticDefenderTravel);
		System.out.println(bestAround.whenAttackEnemyAsFlag);
		System.out.println(bestAround.whenFleeAsFlag);
		System.out.println(bestAround.whenToDirectlyAttackAsAggressor);

		// Controller.doBattle(new Mixed(), new Blitz());
		//Controller.doBattle(new Mixed(), new AggressivePack());
		//Controller.doBattle(new Blitz(), new Mixed());
		//Controller.doBattle(new Human(), new SittingDuck());
		//Controller.doBattle(new Mixed(), new SittingDuck());
		// Controller.doBattle(new CoryMcDonald(), new Blitz());
		// Controller.doBattle(new PrescientMoron(), new SittingDuck());
		//Controller.doBattle(new PrescientMoron(), new Human());
		// System.out.println(Controller.doBattleNoGui(new Mixed2(), new CoryMcDonaldGenetic2()));
		// Controller.doBattle(new Mixed2(), new CoryMcDonaldGenetic2());
		// Controller.doBattle(new CoryMcDonald(), new LucasDorrough_IronMen());
		// Controller.doBattle(new LucasDorrough_IronMen(), new CoryMcDonaldGenetic2());
		// Controller.doBattle(new LucasDorrough_IronMen(), new CoryMcDonald());
		// fullTournament();
	}
}
