import java.util.concurrent.atomic.AtomicStampedReference;


public class Node<T>
{
    /* address,b1,b2 is the bit structure of these
     * b1 refers to flag, which means the 'head' and 'tail' both will be removed
     * b2 refers to tag, which means only 'tail' will be removed
     */
    static final int UF_UT = 0;   // 00
    static final int UF_T  = 1;   // 01
    static final int F_UT  = 2;   // 10
    static final int F_T   = 3;   // 11
    
    
    int key;
    T value;
    int bit;
    volatile AtomicStampedReference<Node<T>> left;
    volatile AtomicStampedReference<Node<T>> right;
    
    // full constructor
    public Node(int key, T value, int bit, Node<T> left, Node<T> right)
    {
        this.key    = key;
        this.value  = value;
        this.bit    = bit;
        this.left   = new AtomicStampedReference<Node<T>>(left,0);
        this.right  = new AtomicStampedReference<Node<T>>(right,0);
    }
    
    // constructor for internals (no value stored)
    public Node(int key, int bit, Node<T> left, Node<T> right) 
    {
        this(key, null, bit, left, right);
    }

    // constructor for leaf. bit value will never be used, since bit comparisons
    // only done on internal nodes
    public Node(int key, T value)
    {
        this(key, value, 0, null, null);
    }
}
