package dk.itu.mario.level.generator;

import java.util.Random;
import java.util.ArrayList;

import dk.itu.mario.MarioInterface.Constraints;
import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.level.CustomizedLevel;
import dk.itu.mario.level.MyLevel;
import JSci.maths.statistics.*;

public class MyLevelGenerator extends CustomizedLevelGenerator implements LevelGenerator{

	
	 //playerInfo
	public float jumper;
	public float collector;
	public float hunter;
	public float destroyer;
	public float rusher;
	
	public float difficulty;
	
	
	public LevelInterface generateLevel(GamePlay playerMetrics) {

		float[] playerEval = new float[4];
		//Get user scores
		PlayerEvaluation(playerMetrics);
		playerEval[0] = jumper;
		playerEval[1] = collector;
		playerEval[2] = hunter;
		playerEval[3] = destroyer;
		
		//Constants
		int levelGenerationCount = 1000;

		//Get user scores
		float[] playerScore = playerEval;

		ArrayList levelCollection = new ArrayList();
		//Generate set of levels 
		for(int i = 0; i < levelGenerationCount; i++) {
			CustomizedLevel level = new CustomizedLevel(320,15,new Random().nextLong(),1,LevelInterface.TYPE_OVERGROUND,playerMetrics,playerEval);
			levelCollection.add(level);
			//debugPrintLevel(level);
		}
		
		//Pick best level using simulated annealing
		LevelInterface bestLevel = simulatedAnnealing(levelCollection,playerScore);
		debugPrintLevel((CustomizedLevel)bestLevel);

		//level.getBlock();
		return bestLevel;
	}
	
	public LevelInterface simulatedAnnealing(ArrayList levelCollection, float[] playerScore) {
		//Constants
		Long seed = new Random().nextLong();
		Random random = new Random(seed);
		double temp = 100;
		double coolingRate = 0.9;
		int neighborRange = levelCollection.size() / 5;

		CustomizedLevel bestLevel = (CustomizedLevel)levelCollection.get(0);
		float currentScore = evaluateLevel(bestLevel, playerScore);
		int currentIndex = 0;

		while(temp > 1){
			int value = (int)random.nextInt(neighborRange);
			int newIndex = currentIndex + value;

			//Range validating
			if(newIndex >= levelCollection.size()) newIndex = levelCollection.size()-1;
			if(newIndex < 0) newIndex = 0;

			CustomizedLevel level = (CustomizedLevel)levelCollection.get(newIndex);
			float randomLevelScore = evaluateLevel(level, playerScore);

			double probability = ((randomLevelScore - currentScore)/temp);
			if(random.nextInt() < probability) {
				currentIndex = newIndex;
				bestLevel = level;
				currentScore = randomLevelScore;
			}

			if(randomLevelScore < currentScore) {
				currentIndex = newIndex;
				bestLevel = level;
				currentScore = randomLevelScore;
			}

			temp *= coolingRate;
		}

		return (LevelInterface)bestLevel;
	}

	public float evaluateLevel(CustomizedLevel level, float[] playerScore) {
		float sum = 0;
		for(int i = 0; i < playerScore.length; i++) {
			sum += Math.abs(level.eval[i] - playerScore[i]);
		}
		return sum;
	}

	public void debugPrintLevel(CustomizedLevel level) {
		System.out.println("Level: \n" +
			       "Jump   :" + level.eval[0] + "\n" +
				   "Coin   :" + level.eval[1] + "\n" +
				   "Monster:" + level.eval[2] + "\n" +
				   "Block  :" + level.eval[3]);
	}

	@Override
	public LevelInterface generateLevel(String detailedInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	public void PlayerEvaluation(GamePlay playerMetrics){
		 //jumper
        System.out.println("*****JUMPER*****");
        
        NormalDistribution jumpND = new NormalDistribution(60.0, 225.0);
        jumper = (float)jumpND.cumulative(playerMetrics.aimlessJumps);
        System.out.println("jumper = " + jumper);
        System.out.println("aimlessJumps: " + playerMetrics.aimlessJumps);

        //collector
        System.out.println("*****COLLECTOR*****");
        
        float percentageCoinsCollected = (float)playerMetrics.coinsCollected/(float)playerMetrics.totalCoins;
        float percentageCoinsBlocksDestroyed = (float)playerMetrics.coinBlocksDestroyed/(float)playerMetrics.totalCoinBlocks;
        float percentageMixedCoinsCollected = (0.6f*percentageCoinsCollected + 0.4f*percentageCoinsBlocksDestroyed);
        NormalDistribution collectND = new NormalDistribution(0.5, 0.0256);
        collector = (float)collectND.cumulative(percentageMixedCoinsCollected);  
        System.out.println("collector = " + collector);
        System.out.println("percentage of mixed coins collected: " + percentageMixedCoinsCollected);
        System.out.println("coins collected: " + playerMetrics.coinsCollected);
        System.out.println("total coins: " + playerMetrics.totalCoins);
        System.out.println("percentage of coins collected: " + percentageCoinsCollected);
        System.out.println("coin blocks destroyed: " + playerMetrics.coinBlocksDestroyed);
        System.out.println("total coin blocks: " + playerMetrics.totalCoinBlocks);
        System.out.println("percentage coin blocks destroyed: " + percentageCoinsBlocksDestroyed);
        
        
        //Hunter
        System.out.println("*****HUNTER*****");
        float enemiesKilled = (float)(playerMetrics.RedTurtlesKilled+playerMetrics.GreenTurtlesKilled
	        	+playerMetrics.ArmoredTurtlesKilled+playerMetrics.GoombasKilled
	        	+playerMetrics.CannonBallKilled+playerMetrics.JumpFlowersKilled
	        	+playerMetrics.ChompFlowersKilled);
        float percentageEnemiesKilled = enemiesKilled / (float)playerMetrics.totalEnemies;
        
        NormalDistribution huntND = new NormalDistribution(0.4, 0.0225);
        hunter = (float)huntND.cumulative(percentageEnemiesKilled);
        
        
        System.out.println("hunter: " + hunter);
//        System.out.println("RedTurtlesKilled: " + playerMetrics.RedTurtlesKilled);
//        System.out.println("GreenTurtlesKilled: " + playerMetrics.GreenTurtlesKilled);
//        System.out.println("ArmoredTurtlesKilled: " + playerMetrics.ArmoredTurtlesKilled);
//        System.out.println("GoombasKilled: " + playerMetrics.GoombasKilled);
//        System.out.println("CannonBallKilled: " + playerMetrics.CannonBallKilled);
//        System.out.println("JumpFlowersKilled: " + playerMetrics.JumpFlowersKilled);
//        System.out.println("ChompFlowersKilled: " + playerMetrics.ChompFlowersKilled);
        System.out.println("percentageEnemiesKilled: " + percentageEnemiesKilled);
        System.out.println("enemiesKilled: " + enemiesKilled);
        System.out.println("totalEnemies: " + playerMetrics.totalEnemies);
        
        
        
        //Destroyer
        System.out.println("*****DESTROYER*****");
        float percentageEmptyBlocksDestroyed = (float)playerMetrics.emptyBlocksDestroyed
        		/(float)playerMetrics.totalEmptyBlocks;
        NormalDistribution destroyND = new NormalDistribution(0.4, 0.04);
        destroyer = (float)destroyND.cumulative(percentageEmptyBlocksDestroyed);
        System.out.println("destroyer = " + destroyer);
        System.out.println("empty blocks destroyed: " + playerMetrics.emptyBlocksDestroyed);
        System.out.println("total empty blocks: " + playerMetrics.totalEmptyBlocks);
        
      //Rusher
        System.out.println("*****RUSHER*****");
        
        NormalDistribution rushND = new NormalDistribution(100, 900);
        rusher = (float)rushND.cumulative(200f-playerMetrics.completionTime);
        System.out.println("rusher = " + rusher);
        System.out.println("completionTime = " + playerMetrics.completionTime);
		difficulty = rusher + 1f;
	}
	
	
}
