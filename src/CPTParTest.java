import java.util.Random;

/**
 * This class is used to test the performance of the CPT
 * within a parallel environment
 * 
 * Derived from LockFreeBSTTest by 
 * 
 * @author klang
 *
 */

public class CPTParTest implements Runnable
{
    // info for how the tests are actually run
    int threadID;
    public static int NUM_THREADS;
    public static int getPercent, insPercent, delPercent;
    public static int iterations, keyRange, initSize;
    
    // all threads have a static reference to a shared cpt object
    static ConcurrentPatriciaTrie<Integer> cpt;
    
    // info for how the tests actually perform
    static long timeSpent[];
    static double ops;
  
    
    public CPTParTest(int id)
    {
        this.threadID = id;
    }
    
    @Override
    public void run()
    {
        Random r = new Random();
        long start = System.currentTimeMillis();
        
        for(int i = 0; i < iterations; i++)
        {
            int op = r.nextInt(100);
            
            if(op < getPercent)    // [0, GP)
            {
                cpt.get(r.nextInt(keyRange)+1);
            }
            else if(op < getPercent + insPercent)   // [GP, GP+IP)
            {
                // doesn't matter what we insert
                cpt.insert(r.nextInt(keyRange)+1, 1);
            }
            else    // [GP+IP, 100]
            {
                cpt.delete(r.nextInt(keyRange)+1);
            }
        }
        
        timeSpent[threadID] = System.currentTimeMillis()-start;
    }
    
    public static void main(String[] args) throws InterruptedException
    {
        // loop through all thread numbers
        for(NUM_THREADS = 1; NUM_THREADS <= 16; NUM_THREADS *= 2)
        {
            // set up testing environment vars
            getPercent = 90;
            insPercent = 9;
            delPercent = 1;
            iterations = 5000000 / NUM_THREADS;   // how many iterations each thread does
            keyRange = 1000000;
            initSize = 0;
            timeSpent = new long[NUM_THREADS];
            cpt = new ConcurrentPatriciaTrie<Integer>();
            
            // prepopulate
            Random r = new Random();
            for(int i = 0; i < initSize; i++)
            {
                cpt.insert(r.nextInt(keyRange)+1, 1);
            }
            
            // create and start threads
            Thread[] thread = new Thread[NUM_THREADS];
            for(int i = 0; i < thread.length; i++)
            {
                thread[i] = new Thread(new CPTParTest(i));
                thread[i].start();
            }
            
            // wait till threads finish
            for(int i = 0; i < thread.length; i++)
            {
                thread[i].join();   // block until thread finishes
            }
            
            // collect and compute data here
            long totalTimeSpent = 0;
            for(long time : timeSpent)
            {
                totalTimeSpent += time;
            }
            double avgTime = (totalTimeSpent / 1000.0d) / (NUM_THREADS);
            ops = (NUM_THREADS * iterations) / avgTime;
            
            System.out.printf("get/ins/del: %d / %d / %d\n", getPercent, insPercent, delPercent);
            System.out.printf("Key range: %d\n", keyRange);
            System.out.printf("Num threads: %d\n", NUM_THREADS);
            System.out.printf("Throughput (op/sec): %f\n", ops);
            System.out.println();
        }
    }

}
