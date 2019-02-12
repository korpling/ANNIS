# Create new query builder

A query builder is a class that
1. implements the `QueryBuilderPlugin` interface
2. has the `@PluginImplementation` annotation

When implementing the interface you have to provide a short name, a caption
and a callback function that creates new Vaadin components. You get an object
of the type `QueryController` which you can use to set new queries from your
component.

A query builder plugin must be either registered in the `addCustomUIPlugins()` function
of the `SearchUI` (if the plugin is part of the annis-gui project) or must be
added to a JAR-file that is located at one of the following locations:
- the plugins folder inside the deployed web application
- the path defined in the `ANNIS_PLUGINS` environment variable

Please also note that it is possible to configure a default query builder for an
instance. Further information can be found in the User Guide.