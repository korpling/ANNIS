
Ext.onReady(function(){
  //The Search Window

	
  Ext.QuickTips.init();

  // turn on validation errors beside the field globally
  Ext.form.Field.prototype.msgTarget = 'side';
		
  var formPanelLogin = new Ext.FormPanel({
    labelWidth: 75, // label settings here cascade unless overridden
    frame:true,
    header: false,
    bodyStyle:'padding:5px 5px 0',
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
          if(o.getKey()==13)
          {
            formPanelLogin.getForm().getEl().dom.submit();
          }
        }
      }

    }],
    buttons: [{
      id: 'btnLogin',
      text: 'Login',
      handler: function() {
        formPanelLogin.getForm().getEl().dom.submit();
      }
            		
    }],
    onSubmit: Ext.emptyFn,
    submit: function() {
      this.getForm().getEl().dom.submit();
    }
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
