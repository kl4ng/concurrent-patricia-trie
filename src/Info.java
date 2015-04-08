
public abstract class Info<T> { }

class Unflag<T> extends Info<T> { 
	public Unflag() { }
}

class Flag<T> extends Info<T> {
	Info<T> oldInfo1;
	Info<T> oldInfo2;
	
	Node<T> par;
	Node<T> oldChild;
	Node<T> newChild;

	// volatile because this can be set by any thread once the Flag object is 'help'ed
	volatile boolean flagDone;
	
	public Flag(Info<T> oldInfo1, Info<T> oldInfo2, Node<T> par,
			Node<T> oldChild, Node<T> newChild) {
		this.oldInfo1 = oldInfo1;
		this.oldInfo2 = oldInfo2;
		this.par = par;
		this.oldChild = oldChild;
		this.newChild = newChild;
		
		// this is set once the flag object is actually used, not created
		this.flagDone = false;
	}
}
