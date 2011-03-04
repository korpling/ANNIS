
Ext.onReady(function(){
  
  //The Search Window
  Ext.QuickTips.init();

  // turn on validation errors beside the field globally
  Ext.form.Field.prototype.msgTarget = 'side';

  var loginSubmitConfig = {
    method:'POST',
    waitTitle:'please wait',
    waitMsg:'login data is being sent to server',
    success: function(f,o)
    {
      // redirect to index.html
      window.location = 'search.html';
            
    },
    failure:function(f,o)
    {
      Ext.MessageBox.show(
      {
        title: 'Login error',
        msg: 'Wrong username or password.',
        icon: Ext.MessageBox.ERROR,
        buttons: Ext.MessageBox.OK
      });
    }
  };
	
  var formPanelLogin = new Ext.FormPanel({
    labelWidth: 75, // label settings here cascade unless overridden
    frame:true,
    header: false,
    width: 350,
    defaults: {
      width: 250
    },
    defaultType: 'textfield',
    items: [{
      id: 'username',
      fieldLabel: 'User Name',
      name: 'user',
      allowBlank:true
    },{
      id: 'password',
      fieldLabel: 'Password',
      name: 'password',
      inputType: 'password',
      allowBlank:true,
      listeners:{
        specialkey:function(f,o){
          if(o.getKey()===13)
          {
            formPanelLogin.getForm().submit(loginSubmitConfig);
          }
        }
      }

    }],
    buttons: [{
      id: 'btnLogin',
      text: 'Login',
      handler: function() {
        formPanelLogin.getForm().submit(loginSubmitConfig);
      }
            		
    }],
    buttonAlign:'center',
    url: conf_context + '/LoginLogout'
  });

  var windowLoginForm = new Ext.Window({
    title: 'Annis&sup2; Login',
    closable:false,
    maximizable: false,
    width:375,
    height:150,
    //border:false,
    plain:true,
    layout: 'fit',
    closeAction: 'hide',
    items: [formPanelLogin]
  });
  windowLoginForm.show();
  
});
