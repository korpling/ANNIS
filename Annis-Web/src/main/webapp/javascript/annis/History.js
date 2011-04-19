Ext.onReady(function()
{
  History = function()
  {

    // need this pointer for event-function
    var finalThis = this;

    var updateDropDownMenu = function()
    {
      var items = finalThis.splitButton.menu.items;
      const
      MAX_MENU_ITEMS = 5;

      // only insert if, store has really stored a new element
      if ((items === undefined) || (store.getCount() > items.getCount()))
      {
        finalThis.splitButton.menu.insert(0, {
          text : (store.getAt(0)).data['query'],
          handler : function(b, e)
          {
            Ext.getCmp('queryAnnisQL').setValue(b.text);
          }
        });
      } else
      {
        /**
         * if this query is already in the menu, the menu is cleared. This might
         * be inefficient for big histories.
         * 
         */
        finalThis.splitButton.menu.removeAll();
        var recordsArray = store.getRange([ 0 ], [ MAX_MENU_ITEMS - 1 ]);
        // count the other way around for ascending sorted menu
        for ( var i = recordsArray.length - 1; i >= 0; i--)
        {
          finalThis.splitButton.menu.insert(0, {
            text : recordsArray[i].data['query'],
            handler : function(b, e)
            {
              Ext.getCmp('queryAnnisQL').setValue(b.text);
            }
          });
        }
      }

      // delete least added element
      if (items !== undefined && items.getCount() > MAX_MENU_ITEMS)
        finalThis.splitButton.menu.remove(items.last());

    };

    // initiate GridView for all queries
    var store = new Ext.data.ArrayStore({
      fields : [ {
        name : 'query'
      } ],
      listeners : {
        'add' : updateDropDownMenu
      }
    });

    var grid = new Ext.grid.GridPanel({
      store : store,
      columns : [ {
        header : '#',
        width : 30,
        renderer : function(value, metadata, record, rowIndex, colIndex)
        {
          return (rowIndex + 1);
        }
      }, {
        id : 'query',
        header : 'Query',
        sortable : true,
        width : 330,
        dataIndex : 'query'
      }, {
        header : "url",
        width : 30,
        dataIndex : "id",
        renderer : function()
        {
          var action = 'getCitationWindow()';
          var linkStart = "<a href='#' onclick='" + action + "'><img src='";
          var linkEnd = "/images/url.png'></a>";
          return linkStart + conf_context + linkEnd;
        }
      } ],
      sm : new Ext.grid.RowSelectionModel({
        singleSelect : true,
        store : store,
        listeners : {
          'rowselect' : function(row, rowIndex, record)
          {
            Ext.getCmp('queryAnnisQL').setValue(record.get('query'));
          }
        }
      }),
      viewConfig : {
        rowOverCls : 'history-hover'
      },
      stripeRows : true,
      width : 400,
      height : 300
    });

    var historyWindow = function()
    {
      new Ext.Window({
        autoDestroy : true,
        title : 'history',
        width : 400,
        height : 300,
        items : grid,
        closeAction : 'hide',
        stateful : true,
        stateId : 'grid'
      }).show();
    };

    this.update = function(lastQuery)
    {
      // insert query into arraystore and use query for index
      store.insert(0, new store.recordType({
        query : lastQuery
      }, lastQuery));

      grid.getView().refresh(false);
    };

    this.splitButton = new Ext.SplitButton({
      fieldLabel : 'History',
      text : 'Query History',
      menu : new Ext.menu.Menu(),
      listeners : {
        'click' : historyWindow
      }
    });
  };
});