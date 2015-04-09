import static org.junit.Assert.*;

import org.junit.Test;


public class ConcurrentPatriciaTrieTest {

    @Test
    public void whenInitialized() {
        ConcurrentPatriciaTrie<Integer> cpt =  new ConcurrentPatriciaTrie<Integer>();
        assertFalse(cpt.contains("ayy"));
        assertTrue(cpt.contains(""));
    }
}
