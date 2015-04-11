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

        this.rootChild = new Node<T>(key, null);        
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
            
            // find where we want to insert
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
            
            // check if we have already have this key inserted
            if(node.key == key)
            {   
                return false;
            }
            
            // we have the closest thing to the key, find closest bit
            // that doesnt match, that will be this new internal bit val
            bit = 0;
            while(isSet(key, bit) == isSet(node.key, bit))
            {
                bit++;
            }
            
            /* Insert new internal with children of 'node' and 'newNode'
             * The left child being the one where the different bit is set to 0
             * and the right child being the one where the differing bit is set to 1
             */
            Node<T> internal;
            Node<T> newNode = new Node<T>(key, value);
            if(isSet(key, bit))
            {
                internal = new Node<T>(key, value, bit, node, newNode);
            }
            else
            {
                internal = new Node<T>(key, value, bit, newNode, node);
            }
            
            // insert new internal/leaf pair
            if(isSet(key, pnode.bit))    // go right
            {
                if(pnode.right.compareAndSet(node, internal, 0, 0))
                {
                    return true;
                }
                else    // go left
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
                    if(node == pnode.left.getReference())
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
        
        while(curField != null)
        {
            cur = curField.getReference();
            
            // check if the par->leaf edge is tagged
            if(parField.getStamp() == Node.UF_UT || parField.getStamp() == Node.F_UT) 
            {
                // save that this as the last untagged edge we've seen
                s.ancestor = s.parent;
                s.successor= s.leaf;
            }
            
            // advance parent and leaf as we go down
            s.parent = s.leaf;
            s.leaf = cur;
            parField = curField;
            
            // decide which way we will go down further
            // go right if the bit of key is set, left otherwise
            if(isSet(key, cur.bit))
            {
                curField = cur.right;
            }
            else
            {
                curField = cur.left;
            }    
        }
        // we have found
        return s;
    }
	
    public boolean cleanUp(long key, SeekRecord s)
    {
        return true;
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
