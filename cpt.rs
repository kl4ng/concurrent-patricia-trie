// Kevin Lang
// Cole Garner
//
// Concurrent Patricia trie with managed memory instead of
// garbage collection. Based off of the following paper:
// http://www.temple.edu/cis/icdcs2013/data/5000a216.pdf
// by Niloufar Shafiei

use std::sync::Arc;
use self::Node::*;

pub struct CPT {
    root: Arc<Node>
}

enum Node {
    Leaf { 
        label:  String,
        info:   Info,
    },

    Internal {
        label:  String,
    }
}

enum Info {
    Flag {
        test:   Arc<Node>,
    },

    Unflag,
}


impl CPT {
    fn new() -> CPT {
        CPT {
            root: Arc::new(Leaf{label: String::new(), info: Info::Unflag}),
        }
    }
}

fn main() {

}
