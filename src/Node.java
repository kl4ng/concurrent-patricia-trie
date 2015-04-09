
public class Node<T> {
	final String key;
	final T val;
	final int bit;
	
	// these are volatile since they are the fields we are CASing
	volatile Info<T> info;
	volatile Node<T> left, right;
	
	public Node(String key, T val, int bit, Info<T> info, Node<T> left, Node<T> right) {
		this.bit = bit;
		this.key = key;
		this.val = val;
		
		this.info = info;
		this.left = left;
		this.right = right;
	}
	
	public Node(String key, T val) {
		this(key, val, 0, null, null, null);
	}
	
	public Node(final Node<T> node){
		this(node.key, node.val, node.bit, node.info, node.right, node.left);
	}
}
