
public class Node<T> {
	int bit;
	String key;
	T val;
	Info<T> info;
	Node<T> left, right;
	
	public Node() {
		
	}
	
	public Node<T> clone() {
		return new Node<T>();
	}
}
