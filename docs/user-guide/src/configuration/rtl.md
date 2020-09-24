# Right-to-Left Visualizations

The KWIC, grid and tree visualizers support right to left layouting of Arabic and
Hebrew characters. As soon as such a character is recognized in a search result, the
visualization is switched into right-to-left mode for these visualizers. If this behavior is
not desired (e.g. a left-to-right corpus with only a few incidental uses of such
characters), this behavior can be switched off for the entire ANNIS instance by setting:

~~~
annis.disable-rtl=true
~~~
in the file `application.properties` in one of the configuration locations.