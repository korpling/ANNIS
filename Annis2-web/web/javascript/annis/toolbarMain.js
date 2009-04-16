/*
 * Ext JS Library 2.0.1
 * Copyright(c) 2006-2008, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */

/** opens a  new tutorial window */
function showTutorial()
{
  var windowTutorial = new Ext.Window({
    html: '<iframe width="100%" height="100%" src="' + conf_context + '/tutorial/index.html"',
    layout: 'fit',
    maximizable: true,
    width: 600,
    height: 500            
  });
  windowTutorial.show();
}

Ext.onReady(function()
{
  Ext.QuickTips.init();
  
  var menuAnnis = new Ext.menu.Menu({
    id: 'mainMenu',
    items: [
      {
        text: 'Logout',
        handler: function() {
          Ext.Msg.show({
            title:'Logout',
            msg: 'Do you really want to logout from ANNIS&sup2;?',
            buttons: Ext.Msg.YESNO,
            fn: function (btn) {
              //logout if confirmed
              if(btn == 'yes') {
                window.location.href="?logout";
              }
            },
            icon: Ext.MessageBox.QUESTION
          });
        }
      },
      '-',
      {
        text: 'About ANNIS&sup2;',
        handler: function() {
          Ext.Msg.show({
            title:'About ANNIS&sup2;',
            msg: '<img src=\"images/annis-logo.jpg\" /><br /><br />' + 
              'Annis&sup2; is a project of the ' +
              '<a href=\"http://www.sfb632.uni-potsdam.de/\" target=\"_blank\">SFB 632</a><br /><br />'+
              'Software revision: ' + conf_revision,
            buttons: Ext.Msg.OK,
            icon: Ext.MessageBox.INFO
          });
        }
      }
    ]
  });

  var toolbarMain = new Ext.Toolbar();
  toolbarMain.render('toolbar');

  labelUser = new Ext.Toolbar.TextItem('fetching username...');
  toolbarMain.add({
      text:'ANNIS&sup2;',
      menu: menuAnnis
    },
    {
      text: 'Tutorial',
      handler: showTutorial
    },
    '->',
    labelUser);
    
  // try to get username
  Ext.Ajax.request(
  {
    url: conf_context + '/secure/SessionInfo',
    method: 'GET',
    params: {what:'username'},
    success: function(response) 
    {
        
      labelUser.getEl().innerHTML = 'logged in as \"' + response.responseText + "\"";
    },
    failure: function()
    {
      labelUser.getEl().innerHTML = 'error, maybe you are not logged in properly';
        
    }
  }
);
});