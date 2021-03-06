# Administration Interface

The administration interface allows you to import and delete corpora, as well as to
define users from the ANNIS web application directly. To log into the administration
interface, use the URL of you ANNIS web application and add /admin or press the Administration button next to ‘About ANNIS’ (this button is only
shown if you are a logged in administrator or when using the desktop version). You should see the screen below:

![import in web-adminstration interface](web-admin-import.png)

Here you can upload a zipped relANNIS corpus for import and set the e-mail address
in the configuration file. The corpus management tab allows you to select corpora for
deletion:

![select corpora for deletion](web-admin-corpus-select.png)

And finally the user and group management tabs allow you to add or remove users,
give them special permissions, and determine which groups can see which corpora.

![group management](web-admin-groups.png)

![user management](web-admin-users.png)

These functions edit the files described in the [User Configuration on Server section](../configuration/user.md).