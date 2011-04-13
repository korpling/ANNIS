Ext.onReady(function()
{
  History = function(lastQuery)
  {
    this.update = function(lastQuery)
    {
      var items = this.splitButton.menu.items;
      var entry = {
        text : lastQuery,
        handler : function(b, e)
        {
          Ext.getCmp('queryAnnisQL').setValue(b.text);
        }
      };

      this.splitButton.menu.insert(0, entry);

      if (items !== undefined && items.getCount() > 5)
      {
        this.splitButton.remove(items.last());
      }
    };

    this.splitButton = new Ext.SplitButton({
      fieldLabel : 'History',
      text : 'Query History',
      menu : new Ext.menu.Menu()
    });
  };
});