PSL [![Stable Docs](https://img.shields.io/badge/docs-stable-brightgreen.svg)](https://linqs-data.soe.ucsc.edu/psl-docs/master-head/index.html) [![Develop Docs](https://img.shields.io/badge/docs-develop-orange.svg)](https://linqs-data.soe.ucsc.edu/psl-docs/develop-head/index.html)
===

Probabilistic soft logic (PSL) is a probabilistic programming language for reasoning about
relational and structured data that is designed to be highly scalable. More information about PSL
is available at the [PSL homepage](http://psl.cs.umd.edu).

Building Models
---------------

If you want to use PSL to build models, you probably do not need this source code. Instead,
visit the [Getting Started guide](../../wiki/Getting-Started) to learn
how to create PSL projects that will automatically install a stable version of these libraries.

Installation
------------

If you do want to install PSL from source, you can use [Maven](https://maven.apache.org/) 3.x.
In the top-level directory of the PSL source (which should be the same directory that holds this
README), run

	mvn install

Citing PSL
----------

We hope you find PSL useful! If you have, please consider citing PSL in any related publications as

	@article{bach:arxiv15,
	 Title = {Hinge-Loss Markov Random Fields and Probabilistic Soft Logic},
	 Author = {Bach, Stephen H. and Broecheler, Matthias and Huang, Bert and Getoor, Lise},
	 Volume = {arXiv:1505.04406 [cs.LG]},
	 Year = {2015}}

Additional Resources
====================

* [PSL homepage](http://psl.cs.umd.edu)

* [PSL source repository](https://github.com/linqs/psl)

* [PSL wiki](../../wiki)

* [Getting Started guide](../../wiki/Getting-Started)

* [User group](https://groups.google.com/forum/#!forum/psl-users)
