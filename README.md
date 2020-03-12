# VEL-Explanation
This repository contains research code related to planning with violation enumeration language (VEL), and object-oriented extension to linear temporal logic (LTL), in deterministic Markov Decision Process (MDP) environments.

The code corresponds to our ICAPS 2020 paper, "Generating Explanations for Temporal Logic Planner Decisions" (link not yet available, but forthcoming). Please cite this paper if you use our code in your research work.

Everything in the "main" directory is our own code.  For your convenience, we are also including code corresponding to [Rabinizer 3.1](https://www7.in.tum.de/~kretinsk/rabinizer3.html), which was developed by Jan Kretinsky.  Our code doesn't actually build Rabin automata (because safe and co-safe temporal logic formulas), but uses several of their constructs for convenience.

Our code includes Scott Livingston's fork of [scheck](https://github.com/slivingston/scheck).  The original scheck was created by Timo Latvala.  Our code calls scheck as an external process to construct FSMs from temporal logic formulas.

We're still working on documentation, refactoring, etc, for this repository. For now, if you have any questions, please contact Daniel Kasenberg at dmk@cs.tufts.edu.