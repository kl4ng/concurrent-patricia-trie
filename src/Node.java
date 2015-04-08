
public class Node<T> {
	int bit;
	String key;
	T val;
	
	// these are volatile since they are the fields we are CASing
	volatile Info<T> info;
	volatile Node<T> left, right;
	
	public Node() {
		
	}
	
	public Node<T> clone() {
		return new Node<T>();
	}
}
