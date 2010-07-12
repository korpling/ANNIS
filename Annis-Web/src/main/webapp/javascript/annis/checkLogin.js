
Ext.onReady(function(){
  // check login
  Ext.Ajax.request(
  {
    url: conf_context + '/secure/SessionInfo',
    method: 'GET',
    params: {
      what:'username'
    },
    success: function(response)
    {
      window.location = 'search.html';
    },
    failure: function()
    {
      window.location = 'login.html';
    }
  });
});
