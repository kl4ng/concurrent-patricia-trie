/**
 * Kevin Lang
 * Cole Garner
 * 
 * With help from Niloufar Shafiei's non-replace code
 *
 * Concurrent Patricia Trie WITHOUT Replace
 * but with Edge-locking?
 */


import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


class SearchResult<T> {
	Node<T> gp;			// grandparent of node, if any
	Node<T> p;			// parent of node, if any
	Node<T> node;		// node we found, if any
	Info<T> gpI;		// info of gp, if any
	Info<T> pI;			// info of p, if any
	boolean keyInTrie;	// whether we found the key
	
	public SearchResult() {
		gp = p = node = null;
		gpI = pI = null;
		keyInTrie = false;
	}
	
	public SearchResult(Node<T> gp, Node<T> p, Node<T> node,
						Info<T> gpI, Info<T> pI, boolean keyInTrie) {
		this.gp = gp;
		this.p = p;
		this.node = node;
		this.gpI = gpI;
		this.pI = pI;
		this.keyInTrie = keyInTrie;
	}
}

/**
 * Able to store any T, but the key is just binary string for now
 * Which is stored simply as an integer (32 bits)
 * 
 */
public class ConcurrentPatriciaTrie<T> {
	
	Node<T> root;
	
	// CAS helper variables
	private static final AtomicReferenceFieldUpdater<Node, Info> infoUpdater =
			AtomicReferenceFieldUpdater.newUpdater(Node.class, Info.class, "info");
	private static final AtomicReferenceFieldUpdater<Node, Node> leftUpdater =
			AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "left");
	private static final AtomicReferenceFieldUpdater<Node, Node> rightUpdater =
			AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "right");
	
	
	public ConcurrentPatriciaTrie() {
		root = new Node<T>(); // init with dummy key?
	}
	
	public boolean insert(String key) {
		Flag<T> I;
		
		while(true) {
			I = null;
			SearchResult<T> res = search(key);
			
			if(!res.keyInTrie) {
				return false;
			}
			
			Info<T> nodeI 	= res.node.info;
			Node<T> copy 	= res.node.clone();
			Node<T> newNode = createNode(copy, new Node<T>(), nodeI);
		}
	}
	
	public boolean delete(String key) {
		while(true) {
			Info<T> I = null;
			SearchResult<T> sr = search(key);
			
			if(!sr.keyInTrie)
				return false;
			
			// calculate the node we found's sibling
			// since it is the sibling, if it is set, we want to go opposite direction
			Node<T> sibling = isSet(key, sr.p.bit+1) ? sr.p.left : sr.p.right;
			
			if(sr.gp != null) {
				//create new flag, call help(I)
			}
			
		}
	}
	
	public boolean find(String key) {
		return search(key).keyInTrie;
	}
	
	public T get(String key) {
		SearchResult<T> sr = search(key);
		
		if(sr.keyInTrie)
			return null;
		return sr.node.val;
	}
	
	// change search type to String later, we can decompose any string
	// into bits for this to work.
	private SearchResult<T> search(String key) {
		Node<T> p 	= null;
		Node<T> gp	= null;
		Info<T> pI	= null;
		Info<T> gpI = null;
		
		Node<T> node = root;
		
		// node must be internal AND still at prefix
		while(!isLeaf(node) && isPrefix(node, key)) {
			// update grandparents before we put in new parent vals
			gp 	= p;
			gpI = pI;
			
			p  = node;
			pI = node.info;
			
			// decide where to go next based on bits, 0=left, 1=right
			// TODO: decide if we need |p.label| instead of utilizing bit
			node = isSet(key, p.bit+1) ? p.right : p.left;
		}
		
		// if it is a leaf, it may be the key we are looking for
		boolean keyInTrie = false;
		if(isLeaf(node)) {
			boolean rmvd = logicallyRemoved(node.info);
			keyInTrie = (node.key.compareTo(key) == 0 && !rmvd);
		}
		
		return new SearchResult<T>(gp, p, node, gpI, pI, keyInTrie);
	}
	
	// this completes any update operations
	private boolean help(Flag<T> flag) {
		boolean doChildCAS = true;
		
		// may need to manually check if it actually succeeded, instead of relying on CAS-return-value
		doChildCAS = infoUpdater.compareAndSet(flag.par, flag.oldInfo1, flag);
		
		// if we haven't CAS-failed yet, flag the other internal nodes (if applicable)
		// a node is leaf iff .left && .right == null, so check either)
		if(doChildCAS && flag.oldChild != null && flag.oldChild.left != null) {
			doChildCAS = infoUpdater.compareAndSet(flag.oldChild, flag.oldInfo2, flag);
		}
		
		// if we succesfully flagged all applicable nodes
		if(doChildCAS) {
			flag.flagDone = true;
			
			// attempt physical removal
			if(flag.oldChild == flag.par.left) {
				leftUpdater.compareAndSet(flag.par, flag.oldChild, flag.newChild);
			} 
			else {
				rightUpdater.compareAndSet(flag.par, flag.oldChild, flag.newChild);
			}
		}
		
		// unflag regardless of success, and return if we succeeded or no.
		infoUpdater.compareAndSet(flag.par, flag, new Unflag<T>());
		return flag.flagDone;
	}
	
	private Node<T> createNode(Node<T> n1, Node<T> n2, Info<T> info) {
		// why does it matter if either is a prefix of the other?
		if(isPrefix(n1, n2.key) || isPrefix(n2, n1.key)) {
			if(info.getClass() == Flag.class) {
				// call help(info) ??
			}
			
			// why null return value in this case?
			return null;
		}
		return null;
		
		// return new Internal whose childrens are node1/node2
	}
	
	private static boolean isLeaf(Node node) {
		return node.left == null && node.right == null;
	}
	
	
	private static boolean logicallyRemoved(Info I) {
		// if its not flagged, of course its not logically removed!
		if(I.getClass() == Unflag.class)
			return false;

		// see if this Node's parents has disowned it, logically
		Flag f = (Flag) I;
		return f.par.left == f.oldChild;
		
	}
	
	private static int MSB = 1 << Character.SIZE-1;
	
	private static boolean isSet(String key, int bitIndex) {
		// invalid key (may need to modify so it can handle empty string
		if(key == null)
			return false;
		
		// find what character we need to look at
		int index 	= (bitIndex / Character.SIZE);
		int bit 	= (bitIndex % Character.SIZE);
		
		// the key is too small for that bit to be set
		if(index >= key.length()) {
			return false;
		}
		
		// We deal with the bits in a BigEndian manner
		int mask = (MSB >>> bit);
		return (key.charAt(index) & mask) != 0;
	}
	
	private static boolean isPrefix(Node n, String key) {
		return isSet(key, n.bit);
	}

	
	// testing code until we move to junit
	public static void main(String[] args) {
		
	}
}
