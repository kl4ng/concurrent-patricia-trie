/**
 * Kevin Lang
 * Cole Garner
 * 
 * With help from Niloufar Shafiei's non-replace code
 *
 * Concurrent Patricia Trie WITHOUT Replace
 * but with Edge-locking?
 * 
 * TODO: complete this edge-based lock implementation
 * TODO: create junit tests (both seq and parallel)
 * TODO: consider doing edge-based replace as well
 */


import java.util.concurrent.atomic.AtomicStampedReference;


public class ConcurrentPatriciaTrie<T> {
	
    final Node<T> root, rootChild;
    
    /* we need one dummy key so root starts as internal
     */
    public ConcurrentPatriciaTrie()
    {
        long key = Long.MAX_VALUE;

        this.rootChild = new Node<T>(key, null, 0, null, null);        
        this.root      = new Node<T>(key, null, 0, rootChild, null);
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
    public boolean insert(long key, T value)
    {
        Node<T> node;
        Node<T> pnode;
        int bit;
        boolean insertRight;
        
        while(true)
        {
            pnode   = root;
            node    = rootChild;
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
            
            // check if we have already have this
            if(node.key == key)
            {   
                return false;
            }
            
            insertRight = isSet(key, node.bit);
            
            // we have the closest thing to the key, find closest bit
            // that doesnt match, that will be this new nodes bit val
            bit = 0;
            while(isSet(key, bit) == isSet(node.key, bit))
            {
                bit++;
            }
            
            // create actual nodes that will be inserted
            Node<T> internal;
            Node<T> tmpLeaf = new Node<T>(key, value, bit);
            if(true)// we chose to go right
            {
                internal = new Node<T>(key, value, bit, null, tmpLeaf);
            }
            else    // we chose to go left
            {
                
            }
            
            // insert new internal/leaf pair
            if(insertRight)
            {
                if(pnode.right.compareAndSet(node, internal, 0, 0))
                {
                    return true;
                }
                else
                {
                    // we have failed to insert
                    // if address has not changed, issue is because node is marked
                    if(node == pnode.right.getReference())
                    { 
                        // lets help who we are conflicting with
                        cleanUp(key, seek(key));
                    }
                }
            }
            else
            {
                if(pnode.left.compareAndSet(node, internal, 0, 0))
                {
                    return true;
                }
                else
                {
                    // we have failed to insert
                    // if address has not changed, issue is because node is marked
                    if(node == pnode.right.getReference())
                    { 
                        // lets help who we are conflicting with
                        cleanUp(key, seek(key));
                    }
                }
            }
            
            // go back to beginning and try to insert again!
        }
    }
    
    public SeekRecord<T> seek(long key)
    {
        AtomicStampedReference<Node<T>> parField;
        AtomicStampedReference<Node<T>> curField;
        Node<T> cur;
        
        // init
        SeekRecord<T> s = new SeekRecord<T>(root, rootChild, rootChild, rootChild.left.getReference());
        
        parField = s.ancestor.left;
        curField = s.successor.left;
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
