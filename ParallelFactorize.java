import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;

/**
 * Parallel factorization class
 * Parallel factorize the 100 largest number less
 * than n * n based on prime numbers
 */
public class ParallelFactorize {
    int k;
    int n;
    int[] primes;
    long base;
    CyclicBarrier cb1;
    CyclicBarrier cb2;
    int maxNumber = 100;
    long[][] factorized_numbers = new long[maxNumber][];
    ArrayList<ArrayList<ArrayList<Integer>>> factor_list;

    /**
     * Constructor
     * primes - prime numbers generated from sieve
     * n - number used to create base for factorization
     * k - number of threads to be used
     */
    public ParallelFactorize (int[] primes, int n, int k) {
      this.k = k;
      this.n = n;
      this.primes = primes;
      this.base = (long) n * n;
      this.cb1 = new CyclicBarrier(k);
      this.cb2 = new CyclicBarrier(k + 1);
    }


    /**
     * Create k number of threads for doing the parallelization
     * Uses a Cyclic Barrier to synchroinize and then returns the factorized numbers
     */
    public long[][] factorize() {
      fill_factor_list();
      for (int i = 0; i < k; i++) {
          new Thread(new Worker(i)).start();
      }
      try {
          cb2.await();
      } catch(Exception e) {
          e.printStackTrace();
      }
      return factorized_numbers;
    }

    /**
     * Create factor_list used for storing factors in the parallel factorization
     * Assign space for 100 ArrayLists each with space of k number of arrayLists
     */
    public void fill_factor_list(){
      factor_list = new ArrayList<ArrayList<ArrayList<Integer>>>(maxNumber);
      for (int i = 0; i < 100; i++) {
          ArrayList<ArrayList<Integer>> factor_list_2 = new ArrayList<ArrayList<Integer>>(k);
          for (int j = 0; j < k; j++) {
              factor_list_2.add(new ArrayList<Integer>());
          }
          factor_list.add(factor_list_2);
      }
    }


    /**
     * Inner Worker class used by threads for doing the factorization
     */
    class Worker implements Runnable {
        int id;

        /**
         * id - unique id for each thread
         */
        public Worker(int id) {
          this.id = id;
        }

        /**
         * Run method used by each thread
         * Partly factorizing each number of total 100
         * The threads will then use a Cyclic Barriere to synchroinize
         * The method the checks each number for missing factors and then gather
         * all factors in the 'factorized' array
         */
        public void run() {
          for (int i = 0; i < 100; i++) {
            long to_factor = base - i;
            factorizeNumber(to_factor);
          }
          try {
            cb1.await();
          } catch (Exception e) {
            e.printStackTrace();
          }
          for (int i = id; i < factor_list.size(); i += k) {
            int len = 0;
            long prod = 1;
            long num = base - i;
            ArrayList<ArrayList<Integer>> factors = factor_list.get(i);
            for(int j = 0; j < factors.size(); j++){
              len += factors.get(j).size();
              for(int k = 0; k < factors.get(j).size(); k++){
                prod *= factors.get(j).get(k);
              }
            }
            long[] factArr = getFactorList(prod, num, len);
            gatherNumbers(factArr, i);
          }
          try {
            cb2.await();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }


        /**
         * Returns array for storing factors
         */
        long[] getFactorList(long prod, long num, int len){
          long[] factArr = new long[len];
          if (prod != num) {
              factArr = new long[len + 1];
              factArr[factArr.length - 1] =  (num/prod);
          }
          return factArr;
        }



        /**
         * Factorize part of prime number
         * Each thread will try to factorize prime number
         * on index equal to id of thread up to size of prime array
         */
        public void factorizeNumber(long to_factor) {
          int primes_ind = id;
          int factor_ind = (int) (base - to_factor);
          ArrayList<Integer> primeFactors = factor_list.get(factor_ind).get(id);
          while (primes_ind < primes.length && Math.pow(primes[primes_ind], 2) <= to_factor) {
              if (to_factor % primes[primes_ind] == 0) {;
                  primeFactors.add(primes[primes_ind]);
                  to_factor /= primes[primes_ind];
              } else {
                  primes_ind += k;
              }
          }

        }


        /**
         * Collect numbers from factor_list and
         * and store facors in array factorized
         */
        public void gatherNumbers(long[] numbers, int ind){
          int counter = 0;
          ArrayList<ArrayList<Integer>> factors = factor_list.get(ind);
          for(int i = 0; i < factors.size(); i++){
            for(int j = 0; j < factors.get(i).size(); j++){
              numbers[counter] = factors.get(i).get(j);
              counter++;
            }
          }

          Arrays.sort(numbers);
          factorized_numbers[ind] = numbers;

        }

    }
}
