# Using Web Fonts in the Interface

The configuration of web fonts is placed within an [instance file](instances.md). 
Thus a web font is applied to a specific instance. If you not want to define
an extra instance, it is possible to add the font configuration to the
default.json file in the *instance* directory. If no *instance*
directory or default.json file exists, create it. Add a property
**font** to the config with the following parameters:

~~~json
{
 ...

 "font" :
 {
   "name" : "foo",
   "url": "https://example.com/foo.css",
   "size": "12pt" // optional
 }
}
~~~

You must also provide a css file, which contains the `@font-face` rule
und is reachable under the defined link in the instance config:

~~~css
@font-face {
  font-family: 'bar';
  font-style: normal;
  font-weight: normal;
  font-size: larger;
  src:
	local('bar'),
	url(bar.woff) format('woff');
}
~~~

Further explantation about the `@font-face` rule is available on the [MDN web docs
](https://developer.mozilla.org/en-US/docs/Web/CSS/@font-face).

If you need to have a different font configuration for the frequency chart
just add a `frequency-font` entry. It has the same structure as `font`.