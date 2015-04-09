
public class Node<T> {
	final int bit;
	final String key;
	final T val;
	
	// these are volatile since they are the fields we are CASing
	volatile Info<T> info;
	volatile Node<T> left, right;
	
	public Node(int bit, String key, T val, Info<T> info, Node<T> left, Node<T> right) {
		this.bit = bit;
		this.key = key;
		this.val = val;
		
		this.info = info;
		this.left = left;
		this.right = right;
	}
	
	public Node(int bit, String key, T val) {
		this(bit, key, val, null, null, null);
	}
	
	public Node(final Node<T> node){
		this(node.bit, node.key, node.val, node.info, node.right, node.left);
	}
}
