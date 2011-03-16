Ext.onReady(function()
{
  Ext.Ajax.defaultHeaders = {
    "Content-Type" : "application/x-www-form-urlencoded; charset=utf-8"
  };

  function readableExample(value, metadata, record, rowIndex, colIndex, store)
  {
    return record.json.name + "=\"" + value + "\"";
  }

  MetaDataWindow = function(id, name)
  {
    var hideAttr = (name === "Search Result") ? true : false;

    var config = {};
    config.title = 'Meta Data for ' + name;
    config.width = 800;
    config.height = 420;

    var grid = null;

    // get datastore
    var storeMeta = new Ext.data.JsonStore({
      url : conf_context + '/secure/MetaData?mID=' + id,
      totalProperty : 'size',
      root : 'metadata',
      id : 'id',
      fields : [ 'key', 'value' ],
      // turn on remote sorting
      remoteSort : true
    });

    var colModel = new Ext.grid.ColumnModel([ {
      header : "Name",
      dataIndex : 'key'
    },

    {
      header : "Value",
      dataIndex : 'value'
    } ]);

    var gridMeta = new Ext.grid.GridPanel({
      ds : storeMeta,
      cm : colModel,
      title : 'meta data',
      loadMask : true,
      viewConfig : {
        forceFit : true,
        autoFill : true
      },
      height : 380,
      flex : 1
    });

    if (!hideAttr)
    {
      var storeNodeAttributes = new Ext.data.JsonStore({
        url : conf_context + '/secure/AttributeList?corpusIds=' + id
            + '&noprefix',
        // turn on remote sorting
        remoteSort : false,
        fields : [ 'name', 'values' ]
      });

      var colModelAttribute = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name"
      }, {
        header : "example",
        dataIndex : "values",
        renderer : readableExample
      } ]);

      var gridAttribute = new Ext.grid.GridPanel({
        ds : storeNodeAttributes,
        cm : colModelAttribute,
        loadMask : true,
        title : 'corpus description',
        viewConfig : {
          forceFit : true,
          autoFill : true
        },
        autoWidth : true,
        height : 380,
        flex : 1
      });

      storeNodeAttributes.setDefaultSort('name', 'asc');
      storeNodeAttributes.load({
        params : {
          corpusIdList : '',
          type : 'node'
        }
      });
    }

    storeMeta.load();

    // init config
    config.items = (!hideAttr) ? [ gridMeta, gridAttribute ] : [ gridMeta ];
    config.autoScroll = true;
    config.layout = {
      type : 'hbox'
    };
    config.shadow = false;

    MetaDataWindow.superclass.constructor.call(this, config);
  };

  Ext.extend(MetaDataWindow, Ext.Window);
});