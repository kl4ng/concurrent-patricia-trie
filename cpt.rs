// Kevin Lang
// Cole Garner
//
// Concurrent Patricia trie with managed memory instead of
// garbage collection. Based off of the following paper:
// http://www.temple.edu/cis/icdcs2013/data/5000a216.pdf
// by Niloufar Shafiei

use std::sync::Arc;
use std::option::Option;

// The CPT simply is a root to the tree
pub struct CPT {
    root: Arc<Node>,
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

// Info can either be a Flag, to designate some sort of change,
// or, if nothing is happening, an Unflag
enum Info {
    Flag(Flag),
    Unflag,
}

// These are used to facilitate insertion/deletion/etc
struct Flag {
    flag:   [Option<Arc<Internal>>; 4], // nodes to be flagged
    oldI:   [Option<Arc<Info>>; 4],     // expected CAS value for flagging
    unflag: [Option<Arc<Internal>>; 2], // nodes to unflag
    par:    [Option<Arc<Internal>>; 2], // nodes whose child changed
    old:    [Option<Arc<Node>>; 2],     // expected children
    new:    [Option<Arc<Node>>; 2],     // new children
    mvLeaf: Option<Arc<Leaf>>,          // leaf that may be flagged
    flagDone: bool,
}

// This is the type returned by search(), which is leveraged by
// most of the methods of the CPT.
// gp = grandparent, p = parent
struct Result {
    gp:     Option<Arc<Internal>>,  // gp of node, if found
    p:      Option<Arc<Internal>>,  // p of node, if found
    node:   Node,                   // node we may have found
    gpI:    Option<Arc<Node>>,      // expected children of gp
    pI:     Option<Arc<Node>>,      // expected children of p
    keyInTrie: bool,                // true if we found node
}

impl CPT {
    fn new() -> CPT {
        CPT {
            root: Arc::new(Node::Internal(Internal{ 
                label: String::new(), 
                info:  Info::Unflag,
                child: [None, None],
            })),
        }
    }

    // This is leveraged for most of the other algorithms, will return
    // <gp, p, node, gpi, pi, keyInTrie>
    fn search(&self, v: String) {
        // p is Internal we may need to flag, pI are their children
        let mut p: Option<Arc<Internal>> = None;
        let mut pI:Option<Arc<Node>> = None; 

        // likewise with grandparents
        let mut gp: Option<Arc<Internal>> = None;
        let mut gpI:Option<Arc<Node>> = None;

        // Node we are looking for
        let mut node = self.root.clone();
        loop {
            match *node {
                Node::Internal(ref i) => {
                    
                },
                _ => break,
            }
        }
        
    }

    fn logicallyRemoved(I: Info) -> bool {
        match I {
            Info::Unflag  => false,
            Info::Flag(f) => true, //TODO
        }
    }
}

fn main() {

}
