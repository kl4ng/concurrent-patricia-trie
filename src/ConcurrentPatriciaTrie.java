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
	
    private final Node<T> grandRoot, root;
    
    private final int all1 = ((1 << (31) >> (31) ) >>> 1);
    private final int all0 = 0;
    private final int rootKey = Integer.MAX_VALUE;
    
    /*
     * Initial state of tree looks like this:
     * 
     *      [gr]
     *   [r]    [d]
     * [d] [d]
     * 
     * gr is the grand-root, needed for seeking, and has a dummy-right node
     * r is the logical root of the tree, but we need two roots for seeking
     * d are dummy nodes, and have the same key as gr and r, which is MAX_INT
     * all four of these nodes are null-valued, and thus 'dont exist'
     */
    public ConcurrentPatriciaTrie()
    {
        this.root = new Node<T>(rootKey, null, 0, new Node<T>(all0, null), new Node<T>(all1, null));        
        this.grandRoot = new Node<T>(rootKey, null, 0, root, new Node<T>(all1, null));
    }
    
    /*
     * returns true if the key maps to a non-null value
     * thus this wont do false-positive on our initial nodes
     */
    public boolean contains(int key)
    {
        return get(key) != null;
    }
    
    public boolean debugContains(int key)
    {
        return rContains(root, key);
    }
    
    private boolean rContains(Node<T> r, int key)
    {
        if(r == null)
            return false;
        
        if(r.key == key)
            return true;
        
        return rContains(r.left.getReference(), key) || rContains(r.right.getReference(), key);
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
            if(node.key > key)
            {
                node = node.left.getReference();
            }
            else
            {
                node = node.right.getReference();
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
                
                if(node.key > key)
                {
                    node = node.left.getReference();
                }
                else
                {
                    node = node.right.getReference();
                }
            }
            
            // check if we have already have this key inserted
            if(isLeaf(node) && node.key == key)
            {   
                return false;
            }
            // TODO: special case where we can replace null value of dummy keys?
            
            // we have the closest thing to the key, find first differing bit
            int tmp = node.key ^ key;
            int i = 0;
            while(tmp != 0)
            {
                tmp >>>= 1;
                i++;
            }
            
            int inKey = key >>> i << i;
            inKey = inKey | (1 << (i-1));
            int inMask = all1 >>> i << i;
            
            // Insert new internal with children of 'node' and 'newNode'
            Node<T> internal;
            Node<T> newNode = new Node<T>(key, value);
            if(node.key > key)
            {
                internal = new Node<T>(inKey, inMask, newNode, node);
            }
            else
            {
                internal = new Node<T>(inKey, inMask, node, newNode);
            }
            
            // insert new internal/leaf pair
            if(pnode.key > key)
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
            else
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
            if(cur.key > key)
            {
                curField = cur.left;
            }
            else
            {
                curField = cur.right;
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
        return ((node.key ^ key) & node.mask) == 0;
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
