import static org.junit.Assert.*;

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
        for(int i = 1; i < 100; i++)
            cpt.insert(i,i);
        for(int i = 1; i < 100; i++)
            assertTrue(cpt.contains(i));
    }
    
    @Test
    public void whenInsertingNegative()
    {
        ConcurrentPatriciaTrie<Integer> cpt =  new ConcurrentPatriciaTrie<Integer>();
        for(int i = -100; i < -5; i++)
            cpt.insert(i,i);
        for(int i = -100; i < -5; i++)
            assertTrue(cpt.contains(i));
    }
}
