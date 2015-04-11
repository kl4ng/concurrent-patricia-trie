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
	
    final Node<T> grandRoot, root;
    
    /*
     * Initial state of tree looks like this:
     * 
     *      [gr]
     *   [r]    [d]
     * [0] [1]
     * 
     * gr right dummy node should never be touched, and can probably just be null
     * r is the logical root of the tree, but we need two roots for seeking
     * 0 is all-0-bits key, 1 is all-1-bits key
     * all four of these nodes are null-valued, and thus 'dont exist'
     */
    public ConcurrentPatriciaTrie()
    {
        // dummy key for all of the init 5 nodes
        int key = Integer.MAX_VALUE;

        this.root = new Node<T>(key, null, 0, new Node<T>(0, null), new Node<T>(-1, null));        
        this.grandRoot = new Node<T>(key, null, 0, root, new Node<T>(key, null));
    }
    
    /*
     * returns true if the key maps to a non-null value
     * thus this wont do false-positive on our initial nodes
     */
    public boolean contains(int key)
    {
        return get(key) != null;
    }
    
    /*
     * searches for node, and returns its value if it is a leaf
     * because all internals are used in traversal, not storage
     */
    public T get(int key)
    {
        Node<T> node = root;
        
        while(!isLeaf(node) && isPrefix(node, key))
        {
            if(key >= node.key)
            {
                node = node.right.getReference();
            }
            else
            {
                node = node.left.getReference();
            }
        }
        
        if(isLeaf(node) && key == node.key)
        {
            return node.value;
        }
        else
        {
            return null;
        }
    }
    
    /*
     * Returns false if key is already in tree
     */
    public boolean insert(int key, T value)
    {
        Node<T> node;
        Node<T> pnode;
        
        while(true)
        {
            pnode   = root;
            node    = root.left.getReference();
            
            // find where we want to insert
            while(!isLeaf(node) && isPrefix(node, key))
            {
                pnode   = node;
                
                if(key >= node.key)
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
            // TODO: special case where we can replace null value of dummy keys?
            
            // we have the closest thing to the key, find closest bit that doesn't match
            int bit = 0;
            while(isSet(key, bit) == isSet(node.key, bit)) // could be more efficient
            {
                bit++;
            }
            
            // calculate internal key (capture only those bits before and containing the differing bit)
            int inKey = key & (-1 << Integer.SIZE - bit-1);
            
            // Insert new internal with children of 'node' and 'newNode'
            Node<T> internal;
            Node<T> newNode = new Node<T>(key, value);
            if(newNode.key >= node.key)
            {
                internal = new Node<T>(inKey, bit-1, node, newNode);
            }
            else
            {
                internal = new Node<T>(inKey, bit-1, newNode, node);
            }
            
            // insert new internal/leaf pair
            if(node.key >= pnode.key)
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
    
    // TODO: modify so that it handles prefix-matching
    public SeekRecord<T> seek(int key)
    {
        AtomicStampedReference<Node<T>> parField;
        AtomicStampedReference<Node<T>> curField;
        Node<T> cur;
        
        // init
        SeekRecord<T> s = new SeekRecord<T>(grandRoot, root, root, root.left.getReference());
        
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
            if(key >= cur.key)
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
	
    public boolean cleanUp(int key, SeekRecord s)
    {
        return true;
    }
    
    /*
     * The mask represents all meaningful bits contained in the internal
     * node's key. if any of those bits differ, it is no longer a prefix
     */
    private static boolean isPrefix(Node node, int key)
    {
        return isSet(node.key, node.bit) == isSet(key, node.bit);
    }
    
    /*
	 * since in this implementation we know no internal nodes exist
	 * with either node.left or node.right as null, we can also just check
	 * either left or right, arbitrarily, instead of both.
	 */
	private static boolean isLeaf(Node node)
	{
		return node.left.getReference() == null;
	}

	
	private static int MSB = 1 << Integer.SIZE-1;
	
	private static boolean isSet(long key, int bit)
	{   
		// We deal with the bitIndex in a BigEndian manner
		long mask = (MSB >>> bit);
		return (key & mask) != 0;
	}
}
