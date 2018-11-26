# Create and configure instances

When multiple corpora from different sources are hosted on one server it is often
still desired to group the corpora by their origin and present them differently.
You should not be forced to have an ANNIS frontend and service installation for
each of this groups. Instead the administrator can define so called instances.

An instance is defined by a JSON file inside the instances sub-folder in one of
the configuration locations. The name of the file also defines the instance name.
Thus the file `instances/falko.json` defines the instance named "falko".

~~~json
{
	"display-name": "Falko",
	"default-querybuilder": "tigersearch",
	"default-corpusset": "falko-essays",
	"corpus-sets": [
	{
		"name": "falko-essays",
		"corpus": [
		"falko-essay-l1",
		"falko-essay-l2"
		]
	},
	{
		"name": "falko-summaries",
		"corpus": [
		"falko-summary-l1",
		"falko-summary-l2"
		]
	}
	],
	"keyboard-layout" : "de",
	"login-on-start": "true"
}
~~~

Each instance configuration can have a verbose display-name which is
displayed in the title of the browser window. `default-querybuilder` defines the
short name of the query builder you want to use. Currently only "tigersearch" and "flatquerybuilder" are
available in the default installation.
The `keyboard-layout` variable is used as the default value for the virtual keyboard of the AQL query box.
If `login-on-start` is set to `true` a login window is shown at each startup of the ANNIS search UI if the user is not logged in yet.

While any user can group corpora into corpus sets for their own, you can define
corpus sets for the whole instance. Each corpus set is an JSON-object with a
name and a list of corpora that belong to the corpus set.

Any defined instance is assigned a special URL at which it can be accessed:
`http://<server>/annis-gui/<instance-name>`. The default instance is
additionally accessible by not specifying any instance name in the URL. You can
configure your web server (e.g. Apache) to rewrite the URLs if you need a more
project specific and less "technical" URL (e.g. `http://<server>/falko`).