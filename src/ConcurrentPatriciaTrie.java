/**
 * Kevin Lang
 * Cole Garner
 * 
 * Concurrent Patricia Trie using edge-flagging
 * 
 * With help from Niloufar Shafiei's patricia trie
 * node-based-flagging non-replace code
 * 
 * And help from Aravind Natarajan / Neeraj Mittal's 
 * lock-free-bst code with edge-based flagging/tagging
 * 
 * TODO: create junit tests (parallel)
 * TODO: consider doing edge-based replace as well
 */


import java.util.concurrent.atomic.AtomicStampedReference;


public class ConcurrentPatriciaTrie<T> {
	
    private final Node<T> grandRoot, root;
    
    private final int all1 = Integer.MAX_VALUE;
    private final int all0 = 0;
    private final int rootKey = Integer.MAX_VALUE;
    
    /*
     * Initial state of tree looks like this:
     * 
     *      [gr]
     *   [r]    [1]
     * [0] [1]
     * 
     * gr is the grand-root, needed for seeking, and has a dummy-right node
     * r is the logical root of the tree, but we need two roots for seeking
     * 0 and 1 are dummy nodes, 0 being all0s and 1 being all1s (AKA max_INT)
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
    
    /*
     * recursively searches ENTIRE tree, for debug purposes
     */
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
                if(pnode.left.compareAndSet(node, internal, Node.UF_UT, Node.UF_UT))
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
                if(pnode.right.compareAndSet(node, internal, Node.UF_UT, Node.UF_UT))
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
    
    public final void delete(int key)
    {
        boolean isCleanup = false;
        SeekRecord<T> s;
        Node<T> par;
        Node<T> leaf = null;
        while(true)
        {
            s = seek(key);
            if(!isCleanup)
            {
                leaf = s.leaf;
                if(leaf.key != key)
                {
                    return; //closest thing we found isnt what we want to delete
                }
                else    // we found it, lets delete it
                {
                    par = s.parent;
                    if(par.key > key)
                    {
                        // flag the left, we want to remove it so it shouldnt already be flagged/tagged
                        if(par.left.compareAndSet(leaf, leaf, Node.UF_UT, Node.F_UT))
                        {
                            // attempt to cleanup now
                            isCleanup = true;
                            if(cleanUp(key, s))
                            {
                                return;
                            }
                        }
                        else    // we failed to flag
                        {
                            if(leaf == par.left.getReference())
                            {
                                // address hasnt changed, so either this node
                                // or its sibling was tagged for deletion
                                cleanUp(key, s);
                            }
                        }
                    }
                    else
                    {
                        // flag the left, we want to remove it so it shouldnt already be flagged/tagged
                        if(par.right.compareAndSet(leaf, leaf, Node.UF_UT, Node.F_UT))
                        {
                            // attempt to cleanup now
                            isCleanup = true;
                            if(cleanUp(key, s))
                            {
                                return;
                            }
                        }
                        else    // we failed to flag
                        {
                            if(leaf == par.right.getReference())
                            {
                                // address hasnt changed, so either this node
                                // or its sibling was tagged for deletion
                                cleanUp(key, s);
                            }
                        }
                    }
                }
            }
            else
            {
                // we already have flagged what we want to delete, finish it
                if(s.leaf == leaf)
                {
                    if(cleanUp(key, s))
                    {
                        // we finished our job
                        return;
                    }
                }
                else
                {
                    // we can no longer see it, its gone! - someone helped us
                    return;
                }
            }
        }
    }
    
    /*
     * Receives stamp and sets the tag bit.
     */
    private int setTag(int stamp)
    {
        switch(stamp)
        {
        case Node.UF_UT:
            stamp = Node.UF_T;
            break;
        case Node.F_UT:
            stamp = Node.F_T;
            break;
        }
        
        return stamp;
    }
    
    private int getFlag(int stamp)
    {
        return (stamp == Node.F_T || stamp == Node.F_UT) ? 1 : 0;
    }
    
    public boolean cleanUp(int key, SeekRecord<T> s)
    {
        Node<T> anc = s.ancestor;
        Node<T> par = s.parent;
        Node<T> oldSuc, sib;
        int oldStamp;
        int sibStamp;
        
        if(par.key > key)   // node is to the left
        {
            if(par.left.getStamp() == Node.F_UT || par.left.getStamp() == Node.F_T)
            {
                // leaf is flagged. tag sibling so no modifications happen to parent
                sib = par.right.getReference();
                sibStamp = setTag(par.right.getStamp());
                par.right.attemptStamp(sib, sibStamp);
                
                // the new successor will be this tagged sibling
                sib = par.right.getReference();
                sibStamp = par.right.getStamp();
                
            }
            else
            {
                // node is not flagged, so the sibling must have been flagged
                // lets help them out with deletion
                sib = par.left.getReference();
                sibStamp = setTag(par.left.getStamp());
                par.left.attemptStamp(sib, sibStamp);
                
                // the new successor will be this tagged node
                sib = par.left.getReference();
                sibStamp = par.left.getStamp();
            }
        }
        else    // node is to the right
        {
            if(par.right.getStamp() == Node.F_UT || par.right.getStamp() == Node.F_T)
            {
                // leaf is flagged. tag sibling so no modifications happen to parent
                sib = par.left.getReference();
                sibStamp = setTag(par.left.getStamp());
                par.left.attemptStamp(sib, sibStamp);
                
                // the new successor will be this tagged sibling
                sib = par.left.getReference();
                sibStamp = par.left.getStamp();
                
            }
            else
            {
                // node is not flagged, so the sibling must have been flagged
                // lets help them out with deletion
                sib = par.right.getReference();
                sibStamp = setTag(par.right.getStamp());
                par.right.attemptStamp(sib, sibStamp);
                
                // the new successor will be this tagged node
                sib = par.right.getReference();
                sibStamp = par.right.getStamp();
            }
        }
        
        // everything from successor to 'sibling' is flagged for removal
        // lets CAS all those away (i.e., remove them)
        if(anc.key > key)
        {
            sibStamp = getFlag(sibStamp);
            oldSuc = anc.left.getReference();
            oldStamp = anc.left.getStamp();
            return(anc.left.compareAndSet(oldSuc, sib, oldStamp, sibStamp));
        }
        else
        {
            sibStamp = getFlag(sibStamp);
            oldSuc = anc.right.getReference();
            oldStamp = anc.right.getStamp();
            return(anc.right.compareAndSet(oldSuc, sib, oldStamp, sibStamp));
        }
    }
    
    public SeekRecord<T> seek(int key)
    {
        AtomicStampedReference<Node<T>> parField;
        AtomicStampedReference<Node<T>> curField;
        Node<T> cur;
        
        // init
        SeekRecord<T> s = new SeekRecord<T>(grandRoot, root, root, root.left.getReference());
        
        parField = s.ancestor.left;
        curField = s.successor.left;
        
        while(curField != null && curField.getReference() != null)
        {
            cur = curField.getReference();
            
            if(!isPrefix(cur, key))
            {
                // if where we are at isn't a prefix of key, then the key is not within the cpt
                return s;
            }
            
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
