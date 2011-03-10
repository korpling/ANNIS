Ext.onReady(function()
{
  Ext.Ajax.defaultHeaders = {
    "Content-Type" : "application/x-www-form-urlencoded; charset=utf-8"
  };

  function readableExample(value, metadata, record, rowIndex, colIndex, store)
  {
    return record.json.name + "=\"" + value + "\"";
  }

  MetaDataWindow = function(id)
  {
    var config = {};

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
      loadMask : true,
      viewConfig : {
        forceFit : true,
        autoFill : true
      },
      autoHeight : true,
      autoWidth : true,
      flex : 1
    });

    var storeNodeAttributes = new Ext.data.JsonStore({
      url : conf_context + '/secure/AttributeList?noprefix',
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
      viewConfig : {
        forceFit : true,
        autoFill : true
      },
      autoHeight : true,
      autoWidth : true,
      flex : 1
    });

    storeMeta.load();
    storeNodeAttributes.setDefaultSort('name', 'asc');
    storeNodeAttributes.load({
      params : {
        corpusIdList : '',
        type : 'node'
      }
    });

    // init config
    config.title = 'Meta Data for id ' + id;
    config.width = 800;
    config.height = 400;
    config.items = [ gridMeta, gridAttribute ];
    config.autoScroll = true;
    config.layout = {
      type : 'hbox',
      align : 'stretchmax'
    };

    MetaDataWindow.superclass.constructor.call(this, config);
  };

  Ext.extend(MetaDataWindow, Ext.Window);
});