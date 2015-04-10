import java.util.concurrent.atomic.AtomicStampedReference;


public class Node<T>
{
    long key;
    T value;
    int bit;
    volatile AtomicStampedReference<Node<T>> left;
    volatile AtomicStampedReference<Node<T>> right;
    
    public Node(long key, T value, int bit, Node<T> left, Node<T> right) 
    {
        this.key    = key;
        this.value  = value;
        this.bit    = bit;
        this.left   = new AtomicStampedReference<Node<T>>(left,0);
        this.right  = new AtomicStampedReference<Node<T>>(right,0);
    }
    
    
    public Node(long key, T value, int bit)
    {
        this(key, value, bit, null, null);
    }
}
