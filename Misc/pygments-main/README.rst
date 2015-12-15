README for Pygments (ANNIS Edition)
====================================

This is a stripped down version of Pygments 2.0.2 (http://pygments.org/)

It has support for AQL syntax an can be used to for
syntax highlighting of AQL in publications and presentations.
All other lexers except for the custom AQL one have been removed to
save space.
The original license (see LICENSE file) still applies.


Example
-------

Run the following (needs Python)

.. code:: sh

   ./pygmentize -f svg -o outfile.svg example.aql


Documentation
-------------

... can be found online at http://pygments.org/

Development
-----------

... takes place on `Bitbucket
<https://bitbucket.org/birkenfeld/pygments-main>`_, where the Mercurial
repository, tickets and pull requests can be viewed.

Continuous testing runs on drone.io:

.. image:: https://drone.io/bitbucket.org/birkenfeld/pygments-main/status.png
   :target: https://drone.io/bitbucket.org/birkenfeld/pygments-main/

The authors
-----------

Pygments is maintained by **Georg Brandl**, e-mail address *georg*\ *@*\ *python.org*.

Many lexers and fixes have been contributed by **Armin Ronacher**, the rest of
the `Pocoo <http://dev.pocoo.org/>`_ team and **Tim Hatch**.
