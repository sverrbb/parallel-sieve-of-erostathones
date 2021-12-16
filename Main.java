import java.util.Arrays;
import java.util.Scanner;
import java.io.File;


/**
 * Main
 */
class Main{

  //Font colors used in testing feedback
  public static final String GREEN = "\u001B[32m";
  public static final String RESET = "\u001B[0m";
  public static final String RED = "\u001B[31m";

  static int n = 0; //parameter used for facorization
  static int k = 0; //k = number of threads to be used
  static int runs = 8; //number of runs for each algorithm

  public static void main(String[] args){


    //Checks that number of arguments is right
    if(args.length != 2){
      System.out.println("The program requires 2 arguments");
      System.out.println("java Main <n> <number of threads>");
      return;
    }

    //Try to take input from command line and assign to variables
    try {
      n = Integer.parseInt(args[0]);
      k = Integer.parseInt(args[1]);
    } catch(Exception e){
      System.out.println("Error! Could not read input arguments");
    }

    //Program require parameter n to be greater then 16
    if (n <= 16){
      System.out.println("Input number n must be greater then 16!");
      System.out.println("Please try again!");
      return;
    }

    //Number of threads equal to number of cores if k is 0
    if(k == 0){
      k = Runtime.getRuntime().availableProcessors();
    }

    //Modes for method timeMeasurements
    int timeRuns = 1;
    int timeMedian = 2;
    int timeAll = 3;

    Scanner input = new Scanner(System.in);
    showMenu();
    System.out.print("\nOption: ");
    int mode = input.nextInt();
    System.out.println();
    switch (mode) {
      case 1:
        System.out.print("\nChoose number of runs: ");
        runs = input.nextInt();
        timeMeasurements(timeRuns);
        break;
      case 2:
        timeMeasurements(timeMedian);
        break;
      case 3:
        testProgram();
        break;
      case 4:
        timeMeasurements(timeAll);
        testProgram();
        break;
      default:
        System.out.println("Mode does not exist");
        break;
    }

  }


  //Menu presented when user run the program
  public static void showMenu(){
    System.out.println("\n**** MENU ****");
    System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");
    System.out.println("1. Get measurements for each run");
    System.out.println("2. Get median measurements");
    System.out.println("3. Run program tests");
    System.out.println("4. Run all");
  }


  //Gets runtime for all algorithms
  public static void timeMeasurements(int timeMode){
    double[] seqSoeTimes = new double[runs];
    double[] parSoeTimes = new double[runs];
    double[] seq_factor_times = new double[runs];
    double[] para_factor_times = new double[runs];

    for(int i = 0; i < runs; i++){

      //Time sequential sieve
      long start = System.nanoTime();
      SequentialSoE seq = new SequentialSoE(n);
      int[] primes = seq.getPrimes();
      long end = System.nanoTime();
      double runtime = ((end - start) / 1000000);
      seqSoeTimes[i] = runtime;
      System.gc();

      //Time parallel sieve
      start = System.nanoTime();
      ParallelSoE para = new ParallelSoE(n, k);
      int[] primes2 = para.getPrimes();
      end = System.nanoTime();
      runtime = ((end - start) / 1000000);
      parSoeTimes[i] = runtime;
      System.gc();

      //Time sequential factorize
      start = System.nanoTime();
      long[][] factors = sequentialFactorize(primes, n);
      end = System.nanoTime();
      runtime = ((end - start) / 1000000);
      seq_factor_times[i] = runtime;
      System.gc();

      //Time parallel factorize
      start = System.nanoTime();
      long[][] factorsP = new ParallelFactorize(primes, n, k).factorize();
      end = System.nanoTime();
      runtime = ((end - start) / 1000000);
      para_factor_times[i] = runtime;
      System.gc();

    }
    //Prints measurements for each run
    if(timeMode == 1){
      getMeasurements(seqSoeTimes, parSoeTimes, seq_factor_times, para_factor_times);
    }
    //Prints median measurements
    if(timeMode == 2){
      getMedianMeasurements(seqSoeTimes, parSoeTimes, seq_factor_times, para_factor_times);
    }
    //Prints measurements for each run and median
    if(timeMode == 3){
      getMeasurements(seqSoeTimes, parSoeTimes, seq_factor_times, para_factor_times);
      getMedianMeasurements(seqSoeTimes, parSoeTimes, seq_factor_times, para_factor_times);
    }
  }


  //Uses the runtime data to print median measurements
  public static void getMedianMeasurements(double[] SeqSieve, double[] parSieve,
                                           double[] seqFac,   double[] parFac){

    //Sorts runtimes
    Arrays.sort(SeqSieve);
    Arrays.sort(parSieve);
    Arrays.sort(seqFac);
    Arrays.sort(parFac);

    //Finds median runtime Sieve
    double seqMedian = SeqSieve[runs/2];
    double parMedian = parSieve[runs/2];
    double speedup = seqMedian / parMedian;

    //Finds median runtime factorization
    double medianSeqFactor = seqFac[runs/2];
    double medianParFactor = parFac[runs/2];
    double speedupFactor = medianSeqFactor / medianParFactor;

    System.out.println("**** MEASUREMENTS ****");
    System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");

    System.out.println("Number of runs: " + runs);
    System.out.println("Number of threads: " + k);
    System.out.println("N = " + n);
    System.out.println();

    //Prints out runtime and speedup
    System.out.println("Sieve");
    System.out.println("• Sequential median: " + seqMedian + " ms");
    System.out.println("• Parallel median: " + parMedian + " ms");
    System.out.println("• Speedup: " + speedup);
    System.out.println();

    //Prints out runtime and speedup
    System.out.println("Factorize");
    System.out.println("• Sequential median: " + medianSeqFactor + " ms");
    System.out.println("• Parallel median: " + medianParFactor + " ms");
    System.out.println("• Speedup median: " + speedupFactor);
    System.out.println();
  }

  //Uses runtime data to print measurements for each run
  public static void getMeasurements(double[] seqSieve, double[] parSieve,
                                     double[] seqFac, double[] parFac){

    for(int i = 0; i < runs; i++){
      System.out.println("Run nr. " + (i+1));
      System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");
      printRuntime("Sequentual Sieve", "Parallel Sieve", seqSieve[i], parSieve[i]);
      printRuntime("Sequentual factorization", "Parallel factorization", seqFac[i], parFac[i]);
      System.out.println();

    }
  }


  //Prints runtime and speedup
  public static void printRuntime(String name1, String name2, double runtimeSeq, double runtimePar){
    System.out.println("• Runtime " + name1 + ": " + runtimeSeq + " ms");
    System.out.println("• Runtime " + name2 + ": " + runtimePar + " ms");
    System.out.println("• Speedup: " + runtimeSeq/runtimePar);
    System.out.println();
  }



  //Run all program tests
  public static void testProgram(){
    System.out.println("**** PROGRAM TESTS ****");
    System.out.println("‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾");

    testSieveOutput();
    testSeqAndParaSieve();
    testFactorization();
    testSeqAndParaFactorization();
    writeFactorsToFile(n);

    System.out.println();
  }


  //Test that sieve gives the correct output
  public static void testSieveOutput(){
    int[] primes1 = new SequentialSoE(n).getPrimes();
    boolean sieveOutput = testSieveOutput(primes1);
    if(sieveOutput){
      System.out.println("Status sieve output test:   [" + GREEN +  "passed" + RESET + "]");
    }else{
      System.out.println("Status sieve output test:   [" + RED +  "failed" + RESET + "]");
    }
  }



  //Test that sieve produce correct output for primes < 100
  public static boolean testSieveOutput(int[] primes){
    int[] testPrimes = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
                        43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
    for(int i = 0; i < testPrimes.length;i++){
      if(primes[i] != testPrimes[i]){
        return false;
      }
    }
    return true;
  }

  //Test that sequential and parallel sieve produce same results
  public static void testSeqAndParaSieve(){
    ParallelSoE para = new ParallelSoE(n, k);
    int[] primes2 = para.getPrimes();
    SequentialSoE seq = new SequentialSoE(n);
    int[] primes1 = seq.getPrimes();
    boolean testSieve = testResults(primes1, primes2);

    if(testSieve){
        System.out.println("Status sieve compare test:  [" + GREEN +  "passed" + RESET + "]");
    }else{
        System.out.println("Status sieve compare test:  [" + RED +  "failed" + RESET + "]");
    }
  }


  //Check sequential and parallel sieve
  public static boolean testResults(int[] sequential, int[] parallel){
    int sLength = sequential.length;
    int pLength = parallel.length;
    //Check length of results
    if(sLength != pLength){
      return false;
    }
    //Check number of each index
    for (int i = 0; i < sLength; i++){
      if(sequential[i] != parallel[i]){
        return false;
      }
    }
    return true;
  }


  //Check that sequential and parallel factorization gives same outptup
  public static void testSeqAndParaFactorization(){

    SequentialSoE seq = new SequentialSoE(n);
    int[] primes1 = seq.getPrimes();

    long[][] factors = sequentialFactorize(primes1, n);
    long[][] factorsP = new ParallelFactorize(primes1, n, k).factorize();

    boolean testFactorization = compareFactorization(factors, factorsP);
    if(testFactorization){
      System.out.println("Status factor compare test: [" + GREEN +  "passed" + RESET + "]");
    }else{
      System.out.println("Status factor compare test: [" + RED +  "failed" + RESET + "]");
    }


  }


  //Test factorization
  public static void testFactorization(){
    SequentialSoE seq = new SequentialSoE(n);
    int[] primes2 = seq.getPrimes();
    long[][] factorsP = new ParallelFactorize(primes2, n, k).factorize();
    if (!checkFactors(n, factorsP)) {
        System.out.println("Status factor output test:  [" + RED +  "failed" + RESET + "]");
        return;
    }
    System.out.println("Status factor output test:  [" + GREEN +  "passed" + RESET + "]");
  }


  //Chekck that sequential and parallel factorization gives same output
  public static boolean compareFactorization(long[][] seq, long[][] para) {
      int sLength = seq.length;
      int pLength = para.length;
      if (sLength != pLength) {
          return false;
      }
      for (int i = 0; i < sLength; i++) {
        if (!Arrays.equals(seq[i], para[i])){
          return false;
        }
      }
    return true;
  }


  // Cheks that factorization was correct
  private static boolean checkFactors(long n, long[][] factors) {
      long base = (long) n * n;
      int index = 0;
      for(int i = 0; i < factors.length; i++){
        long factor = 1;
        for(int j = 0; j < factors[i].length; j++){
          factor *= factors[i][j];
        }
        if (factor != base - index) {
            return false;
        }
        index++;
    }
    return true;

  }


  //Write factors to file
  public static void writeFactorsToFile(int n) {
      int[] primes = new SequentialSoE(n).getPrimes();
      long[][] factorList = sequentialFactorize(primes, n);
      Oblig3Precode ob3p = new Oblig3Precode(n);
      long base = (long) n * n;
      for (long[] num : factorList) {
          for(int i = 0; i < num.length; i++){
            ob3p.addFactor(base, num[i]);
          }
          base -= 1;
      }
      ob3p.writeFactors();
      testFileCreation();

  }

  //Check if file is created
  public static void testFileCreation(){
    if(fileExists()){
      System.out.println("Status file creation test:  [" + GREEN +  "passed" + RESET + "]");
    }else{
      System.out.println("Status file creation test:  [" + RED +  "failed" + RESET + "]");
    }

  }

  //Check if expected file exists
  public static boolean fileExists(){
    boolean existStatus = new File("Factors_" + Integer.toString(n) + ".txt").exists();
    return existStatus;
  }


  //Calculate the factorization of the 100 largest numbers less than N * N
  public static long[][] sequentialFactorize(int[] primes, int n) {
      long max = (long) n * n;
      int length = 100;
      long[][] factors = new long[length][];
      for (int i = 0; i < length; i++) {
          long base = max-i;
          factors[i] = SequentialFactorize.factorize(primes, base);
      }
      return factors;
  }

}
