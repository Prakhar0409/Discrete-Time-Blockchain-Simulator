import java.util.Random;

public class Utilities {

	/**
	 * Get a random double from exponential distribution
	 * */
	public static double getExpRand(double lambda) {		//lamda is the mean
		Random random = new Random();
	    return  Math.log(1-random.nextDouble())/(-lambda);
	}
}
