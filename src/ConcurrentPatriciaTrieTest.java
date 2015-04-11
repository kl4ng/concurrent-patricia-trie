import static org.junit.Assert.*;

import java.util.Random;
import java.util.Stack;

import org.junit.Test;


public class ConcurrentPatriciaTrieTest
{

    @Test
    public void whenInitialized()
    {
        ConcurrentPatriciaTrie<Integer> cpt =  new ConcurrentPatriciaTrie<Integer>();
        assertFalse(cpt.contains(12));
        assertFalse(cpt.contains(0));
    }
    
    @Test
    public void whenInsertingPositive()
    {
        ConcurrentPatriciaTrie<Integer> cpt =  new ConcurrentPatriciaTrie<Integer>(); 
        
        Random r = new Random();
        Stack<Integer> s = new Stack<Integer>();
        
        for(int i = 0; i < 500; i++)
        {
            int tmp = r.nextInt(1000)+1;
            cpt.insert(tmp, tmp);
            assertTrue(cpt.contains(tmp));
            s.push(tmp);
        }
        
        while(!s.isEmpty())
        {
            assertTrue(cpt.contains(s.pop()));
        }        
        assertFalse(cpt.contains(1005));
    }
    
    @Test
    public void whenInsertingDummies()
    {
        // should we allow a way for '0' and INT_MAX to be inserted? for now, no
        ConcurrentPatriciaTrie<Integer> cpt =  new ConcurrentPatriciaTrie<Integer>();
        assertFalse(cpt.contains(0));
        cpt.insert(0, 0);
        assertFalse(cpt.contains(0));
    }
}
