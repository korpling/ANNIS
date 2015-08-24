Error codes of the administrator tools {#admin-errorcodes}
==========

Additional to textual output the administration tools will return an error code when exiting in an error condition.
On Linux you can query the exit code of the last command with
\code{.sh}
echo $?
\endcode
An exit code of "0" means normal exit, everything else is an error indication.


exit code | description
----------|-------------
1         | General error, no specific information.
2         | The "annis.home" property was not set. This means the "ANNIS_HOME" environment variable was not set when the startup script was called.
3         | The location where the "annis.home"/"ANNIS_HOME" variable points to is not an existing directory.
4         | Wrong use of command line arguments.
5         | Could not access the database.
6         | Could not access a file.
50        | Copying corpora from old installation failed.
51        | Deleting a corpus failed because it does not exist.
100       | Could not start the internal REST-server used for the ANNIS service (e.g. because the port was already taken).
101       | There was an internal exception when trying to start the internal REST-server used for the ANNIS service.

