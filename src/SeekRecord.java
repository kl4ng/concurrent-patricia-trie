
public class SeekRecord<T>
{
    Node<T> ancestor;
    Node<T> successor;
    Node<T> parent;
    Node<T> leaf;
    
    public SeekRecord(Node<T> ancestor, Node<T> successor, Node<T> parent, Node<T> leaf)
    {
        this.ancestor   = ancestor;
        this.successor  = successor;
        this.parent     = parent;
        this.leaf       = leaf;
    }
}
