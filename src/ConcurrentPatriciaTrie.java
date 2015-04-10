/**
 * Kevin Lang
 * Cole Garner
 * 
 * With help from Niloufar Shafiei's non-replace code
 *
 * Concurrent Patricia Trie WITHOUT Replace
 * but with Edge-locking?
 * 
 * TODO: utilize edge-based locking
 * TODO: utilize internal nodes if possible for better compression
 * TODO: create junit tests (both seq and parallel)
 */


import java.util.concurrent.atomic.AtomicStampedReference;


public class ConcurrentPatriciaTrie<T> {
	
    final Node<T> root;
    
    /* we need one dummy key so root starts as internal
     */
    public ConcurrentPatriciaTrie()
    {
        long key = Long.MAX_VALUE;
        
        AtomicStampedReference<Node<T>> left = new AtomicStampedReference<Node<T>>(
                new Node<T>(key, null, 0, null, null), 0);
        
        root = new Node<T>(key, null, 0, left, null);
    }
    
    /*
     * returns true if the key maps to a non-null value
     */
    public boolean contains(long key)
    {
        return get(key) != null;
    }
    
    public T get(long key)
    {
        Node<T> node = root;
        int bit = -1;
        
        while(!isLeaf(node) && node.bit > bit)
        {
            bit = node.bit;
            if(isSet(key, node.bit))
            {
                node = node.right.getReference();
            }
            else
            {
                node = node.left.getReference();
            }
        }
        
        if(key == node.key)
        {
            return node.value;
        }
        else
        {
            return null;
        }
    }
    
    /*
     * Returns false if key is not in the tree
     */
    public boolean insert(long key)
    {
        Node<T> node;
        Node<T> pnode;
        int bit;
        boolean insertLeft;
        
        while(true)
        {
            pnode   = root;
            node    = root.left.getReference();
            bit     = -1;
            
            while(!isLeaf(node) && node.bit > bit)
            {
                bit     = node.bit;
                pnode   = node;
                
                if(isSet(key, node.bit))
                {
                    node = node.right.getReference();
                }
                else
                {
                    node = node.left.getReference();
                }
            }
            
            Node oldChild = node;
        }
    }
	
	/* since in this implementation we know no internal nodes exist
	 * with either node.left or node.right as null, we can also just check
	 * either left or right, arbitrarily, instead of both.
	 */
	private static boolean isLeaf(Node node)
	{
		return node.left == null;
	}

	
	private static long MSB = 1 << Long.SIZE-1;
	
	private static boolean isSet(long key, int bit)
	{
		// We deal with the bitIndex in a BigEndian manner
		long mask = (MSB >>> bit);
		return (key & mask) != 0;
	}
}
