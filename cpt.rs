// Kevin Lang
// Cole Garner
//
// Concurrent Patricia trie with managed memory instead of
// garbage collection. Based off of the following paper:
// http://www.temple.edu/cis/icdcs2013/data/5000a216.pdf
// by Niloufar Shafiei

use std::sync::Arc;

// The CPT simply is a root to the tree
pub struct CPT {
    root: Arc<Internal>
}

enum Node {
    Leaf(Leaf),
    Internal(Internal),
}

// In order for Leaf to be both 'type of Node' and a distinct type
// we need to have the actual fields stored in its own struct
// Thus, we have enum value Node::Leaf and the distinct type Leaf
struct Leaf {
    label:  String,
    info:   Info,
}

// See Leaf struct for details as to why we need a struct and
// an enum type.
struct Internal {
    label:  String,
    info:   Info,
    child:  [Option<Arc<Node>>; 2],
}

// Flags are used to mark nodes that will be changed, instead of 
// needing to use locks.
enum Info {
    Flag(Flag),
    Unflag,
}

struct Flag {
    flag:   [Option<Arc<Internal>>; 4],// nodes to be flagged
    oldI:   [Option<Arc<Info>>; 4],     // expected CAS value for flagging
    unflag: [Option<Arc<Internal>>; 2], // nodes to unflag
    par:    [Option<Arc<Internal>>; 2], // nodes whose child changed
    old:    [Option<Arc<Node>>; 2],     // expected children
    new:    [Option<Arc<Node>>; 2],     // new children
    mvLeaf: Option<Arc<Leaf>>,          // leaf that may be flagged
    flagDone: bool,
}

impl CPT {
    fn new() -> CPT {
        CPT {
            root: Arc::new(
                Internal{ label: String::new(), 
                          info:  Info::Unflag,
                          child: [None, None], }
                          ),
        }
    }
}

fn main() {

}
