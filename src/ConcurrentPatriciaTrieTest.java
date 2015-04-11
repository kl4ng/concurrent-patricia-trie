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
    public void whenInserting()
    {
        ConcurrentPatriciaTrie<Integer> cpt =  new ConcurrentPatriciaTrie<Integer>();
        cpt.insert(1,1);
        assertTrue(cpt.contains(1));
    }
}
