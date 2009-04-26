Ext.onReady(function()
{
  Ext.MessageBox.show(
  {
    title: 'Login error',
    msg: 'Wrong username or password.',
    icon: Ext.MessageBox.ERROR,
    buttons: Ext.MessageBox.OK
  });
});