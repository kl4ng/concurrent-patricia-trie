import java.util.concurrent.atomic.AtomicStampedReference;


public class Node<T>
{
    long key;
    T value;
    int bit;
    volatile AtomicStampedReference<Node<T>> left;
    volatile AtomicStampedReference<Node<T>> right;
    
    public Node(long key, T value, int bit, AtomicStampedReference<Node<T>> left, AtomicStampedReference<Node<T>> right) 
    {
        this.key    = key;
        this.value  = value;
        this.bit    = bit;
        this.left   = left;
        this.right  = right;
    }
    
    
    public Node(long key, T value)
    {
        this(key, value, -1, null, null);
    }
}
