Ext.onReady(function()
{
  Ext.Ajax.defaultHeaders = {
    "Content-Type" : "application/x-www-form-urlencoded; charset=utf-8"
  };

  function readableExample(value, metadata, record, rowIndex, colIndex, store)
  {
    return '<p style=\'white-space: normal\'>' + record.json.name + "=\""
        + value + "\"</p>";
  }
  
  function edgeAnnotation(value, metadata, record, rowIndex, colIndex, store)
  {    
    var operator = (record.json.subtype === 'd') ? '>' : '->';
    return '<p style=\'white-space: normal\'> node & node & #1 ' + operator
        + '[' + record.json.name + "=\"" + record.json.values
        + "\"] #2</p>";
  }

  MetaDataWindow = function(id, name)
  {
    var hideAttr = (name === "Search Result") ? true : false;

    var config = {};
    config.title = 'Meta Data for ' + name;
    config.width = (!hideAttr) ? 800 : 400;
    config.height = 420;

    var grid = null;
    
    var selectableCell = new Ext.Template(
        '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} x-selectable {css}" style="{style}" tabIndex="0" {cellAttr}>',
        '<div class="x-grid3-cell-inner x-grid3-col-{id}" {attr}>{value}</div>',
        '</td>'
     );

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
    }, {
      header : "Value",
      dataIndex : 'value',
      renderer : function(value)
      {
        var css = 'white-space: normal; overflow: normal; padding-right: 5px';
        return '<p style=\'' + css + '\'>' + value + '</p>';
      }
    } ]);

    var gridMeta = new Ext.grid.GridPanel({
      ds : storeMeta,
      cm : colModel,
      title : 'meta data',
      loadMask : true,
      viewConfig : {
        forceFit : true,
        autoFill : true,
        templates: {
          cell: selectableCell
        }
      },
      height : 380,
      flex : 1
      });
    
    storeMeta.load();

    if (!hideAttr)
    {
      var storeAttributes = new Ext.data.JsonStore({
        url : conf_context + '/secure/AttributeList?corpusIds=' + id
            + '&noprefix',
        // turn on remote sorting
        remoteSort : false,
        fields : [ 'name', 'values', 'type' ]
      });

      var colModelNodeAnnotations = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name"
      }, {
        header : "example",
        dataIndex : "values",
        renderer : readableExample
      } ]);    
      

      var gridNodeAnnotations = new Ext.grid.GridPanel({
        ds : storeAttributes,
        cm : colModelNodeAnnotations,
        loadMask : true,
        title : 'node annotations',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          templates: {
            cell: selectableCell
          }
        },
        autoWidth : true,
        height : 360        
      });
     
      var storeEdgeAnnotations = new Ext.data.JsonStore({
        remoteSort : false,
        fields : [ 'name', 'values', 'type' ]
      });  
      
      var colModelEdgeAnnotation = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name"
      }, {
        header : "example",
        dataIndex : "values",
        renderer : edgeAnnotation
      } ]);
      
      var gridEdgeAnnotations = new Ext.grid.GridPanel({
        ds : storeEdgeAnnotations,
        cm : colModelEdgeAnnotation,
        loadMask : true,
        title : 'edge annotations',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          templates: {
            cell: selectableCell
          }
        },
        autoWidth : true,
        height : 360   
      });
         
      
      storeAttributes.setDefaultSort('name', 'asc');
      storeAttributes.on("load", function(store, records, options)
      {       
        storeEdgeAnnotations.add(store.getRange(0, store.getCount()));
        storeEdgeAnnotations.filter("type", "edge", true, true);
        store.filter("type", "node", true, true);
      });
      storeAttributes.load();
      
   
     
      var rightPanel = new Ext.Panel({
        layout: 'accordion',
        layoutConfig: {
            animate: true
        },
        items : [ gridNodeAnnotations, gridEdgeAnnotations ],
        flex : 1
      });
    }    
    
    

    // init config
    config.items = (!hideAttr) ? [ gridMeta, rightPanel ] : [ gridMeta ];
    config.autoScroll = true;
    config.layout = {
      type : 'hbox'
    };
    config.shadow = false;
    config.resizable = false;

    MetaDataWindow.superclass.constructor.call(this, config);
  };

  Ext.extend(MetaDataWindow, Ext.Window);
});