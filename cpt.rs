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
    Flag {
        flag:   Vec<Arc<Internal>>, // nodes to be flagged [0,4]
        oldI:   Vec<Arc<Info>>,     // expected CAS value for flagging
        unflag: Vec<Arc<Internal>>, // nodes to unflag [0,2]
        par:    Vec<Arc<Internal>>, // nodes whose child changed [0,2]
        old:    Vec<Arc<Node>>,     // expected children
        new:    Vec<Arc<Node>>,     // new children
        mvLeaf: Option<Arc<Leaf>>,  // leaf that may be flagged [0,1]
        flagDone: bool,
    },

    Unflag,
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
