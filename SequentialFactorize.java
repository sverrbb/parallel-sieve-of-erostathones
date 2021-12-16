import java.util.ArrayList;

/**
 * Sequential factorization class
 */
class SequentialFactorize {

    // calculate the factorization of numbers based on the primes
    public static long[] factorize(int[] primes, long base){
        long facNum = base;
        int start = 0;
        ArrayList<Long> tmp = new ArrayList<Long>();
        while (facNum > 1) {
            boolean factor = false;
            for (int i = start; i < primes.length; i++) {
                if(facNum % primes[i] == 0){
                    tmp.add((long) primes[i]);
                    factor = true;
                    facNum /= primes[i];
                    start = i;
                    break;
                }
            }
            if(!factor){
                tmp.add((long) facNum);
                facNum = 1;
            }
        }
        long[] factorized = new long[tmp.size()];
        for (int j = 0; j < tmp.size(); j++){
          factorized[j] = tmp.get(j);
        }

        return factorized;
    }

}
