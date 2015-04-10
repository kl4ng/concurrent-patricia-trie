# concurrent-patricia-trie
A concurrent non-blocking patricia trie implemented in Java. This trie supports long keys and uses edge-based locking for memory and performance improvements.
This was done as a final project for Dr. Dechev's Parallel Algorithms and Programming (COP4520) class at UCF. This repository also includes a final paper written in LaTeX that goes over the details of the project.

## Compiling
### Java Code
Navigate to the src directory and compile:
```
cd ~/concurrent-patricia-trie/src/
javac ConcurrentPatriciaTrie.java
java ConcurrentPatriciaTrie
```

### Latex
To generate a pdf, install latexmk from your distribution's repository. To compile the latex report we have thus far, simply navigate to the latex directory and run latexmk:
```
cd ~/concurrent-patricia-trie/latex/
latexmk -pdf COP4520.tex
```

##Sources
Everything we used can be found in the latex/references.bib file.
