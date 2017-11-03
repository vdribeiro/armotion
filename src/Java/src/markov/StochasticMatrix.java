package markov;
public final class StochasticMatrix{
	public static void main(String[] args){
		//Our data array 
		int[] numbers = {1,2,3,4,5,6,7,8,9};
		//Our weightings
		double[] weightings = {0.0,0.075,0.1,0.15,0.3,0.15,0.1,0.075,0.05};
		//The number of results to generate
		int numOfResults = 7;
		//Loop for the number of numbers we want to generate
		for(int i=0;i<numOfResults;i++){
			//Retrieve a random number between 0.0 and 1.0
			double choice = Math.random();
			//The current sum of wieghtings	
			double currentSum = 0.0;
			//The result is an index to the data array
			int result = 0;
			//Check the weightings
			for(;result<numbers.length;result++){
				currentSum+=weightings[result];
				if(choice <= currentSum){
					break; //break when we have chosen the right number
				}
			}
			//Print out the chosen number	
			System.out.print(numbers[result]+" , ");
		}
		System.out.println();
	}
}
