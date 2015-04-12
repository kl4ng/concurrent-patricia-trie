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
    public static int iterations, keyRange;
    
    // all threads have a static reference to a shared cpt object
    static ConcurrentPatriciaTrie<Integer> cpt;
    
    // info for how the tests actually perform
    static long timeSpent[];
    static double Mops; // million of operations per second
  
    
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
            
            if(op < getPercent)    // 0-GP
            {
                cpt.get(r.nextInt(keyRange)+1);
            }
            else if(op < getPercent + insPercent)
            {
                // doesn't matter what we insert
                cpt.insert(r.nextInt(keyRange)+1, 1);
            }
            else
            {
                cpt.delete(r.nextInt(keyRange)+1);
            }
        }
        
        timeSpent[threadID] = System.currentTimeMillis()-start;
    }
    
    public static void main(String[] args) throws InterruptedException
    {
        // set up testing environment vars
        NUM_THREADS = 4;
        getPercent = 60;
        insPercent = 20;
        delPercent = 20;
        iterations = 10000000;
        keyRange = 1000;
        timeSpent = new long[NUM_THREADS];
        cpt = new ConcurrentPatriciaTrie<Integer>();
        
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
    }

}
