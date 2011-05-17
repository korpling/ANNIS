Ext.onReady(function()
{
  Ext.Ajax.defaultHeaders = {
    "Content-Type" : "application/x-www-form-urlencoded; charset=utf-8"
  };

  // helper functions
  function killNameSpaces(name)
  {
    var edgeNameArray = name.split(":");
    return edgeNameArray[edgeNameArray.length - 1];
  }

  function isAmbiguous(store, records, field)
  {
    var names = {};
    var i;
    for (i = 0; i < records.length; i++)
    {
      var n = killNameSpaces(records[i].get(field));
      if (n)
      {
        if (names[n] === true)
        {
          return true;
        } else
        {
          names[n] = true;
        }
      }
    }
    return false;
  }

  function hasDominanceEdges(records)
  {
    var i;
    for (i = 0; i < records.length; i++)
    {
      var r = records[i];
      if (r.get("type") === 'edge' && r.get("subtype") === 'd')
      {
        return true;
      }
    }
    return false;
  }

  // filter function for edge types
  var edge_names = {};
  function edgeNames(record, id)
  {

    if (record.get("edge_name"))
    {
      var eName = record.get('edge_name');

      if (Ext.util.Format.trim(eName) !== '')
      {
        record.set('name', eName);
      }
      record.commit();

      if (edge_names.eName === true)
      {
        return false;
      }

      edge_names[eName] = true;
      return true;
    }
    return false;
  }

  // render functions
  function nameRenderer(value, metadata, record, rowIndex, colIndex, store)
  {
    if (isAmbiguous(store, store.getRange(), 'name') === true)
    {
      return value;
    } else
    {
      return killNameSpaces(value);
    }
  }

  function readableExample(value, metadata, record, rowIndex, colIndex, store)
  {
    return '<p style=\'white-space: normal;\'>'
        + killNameSpaces(record.get('name')) + "=\"" + record.get('values')[0]
        + "\"</p>";
  }

  function edgeAnnotation(value, metadata, record, rowIndex, colIndex, store)
  {
    var operator = (record.get('subtype') === "d") ? '>' : '->'
        + killNameSpaces(record.get('edge_name'));
    return '<p style=\'white-space: normal;\'>node & node & #1 ' + operator
        + '[' + killNameSpaces(record.get('name')) + "=\""
        + record.get('values') + "\"] #2</p>";
  }

  function edgeTypes(value, metadata, record, rowIndex, colIndex, store)
  {    
    // filter white-space that come from edgeName() when filter for dominance
    // edges
    var edge_name = killNameSpaces(record.get('edge_name'));
    if(edge_name === " ")
      edge_name = '';
    
    var operator = (record.get('subtype') === "d") ? '>' : '->';
    return '<p style=\'white-space: normal;\'>node & node & #1 ' + operator
        + edge_name + " #2</p>";
  }

  function annotationUrl(value, metadata, record, rowIndex, colIndex, store)
  {
    return "<a href='#' onclick='getCitationWindow()'><img src='"
        + conf_context + "/images/url.png'></a>";
  }

  /**
   * this must be global for using in DOM, this function also works when value
   * is undefined. If value is undefinded the function use Citation.generate()
   * for the Citation window.
   */
  getCitationWindow = function(value)
  {
    if (value === undefined)
    {
      Ext.Msg.alert('Citation',
          '<textarea readonly="f" wrap="virtual" rows="5" cols="60">'
              + Citation.generate() + "</textarea>");
    } else
    {
      Ext.Msg.alert("Citation",
          "<textarea readonly='f' wrap='virtual' rows='5' cols='60'>" + value
              + "</textarea>");
    }
  };

  // listener
  function queryToAnnisQL(row, rowIndex, record)
  {
    // filter html-tags
    var reg = /(\<[a-z]+( [a-z]*\=\'[a-z\-\:\; ]*\')*\>)|(\<\/[a-z]*\>)/g;
    var query;

    switch (row.store.storeId)
    {
    case 'storeEdgeTypes':
      query = edgeTypes(null, null, record, null, null, null);
      break;
    case 'storeEdgeAnnotations':
      query = edgeAnnotation(null, null, record, null, null, null);
      break;
    case 'storeNodeAnnotations':
      query = readableExample(null, null, record, null, null, null);
    }

    Ext.getCmp('queryAnnisQL').setValue(query.replace(reg, ''));
  }

  MetaDataWindow = function(id, name)
  {

    var hideAttr = (name === "Search Result") ? true : false;

    var config = {};
    config.title = 'Meta Data for ' + name;
    config.width = (!hideAttr) ? 800 : 400;
    config.height = 402;

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
        autoFill : true
      },
      height : 370,
      flex : 1
    });

    storeMeta.load();
    var gridPanel = {};

    if (!hideAttr)
    {

      // height of accordion components
      var height = 300;

      var storeAttributes = new Ext.data.JsonStore({
        url : conf_context + '/secure/AttributeList?corpusIds=' + id,
        // turn on remote sorting
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ]
      });

      var storeNodeAnnotations = new Ext.data.JsonStore({
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ],
        storeId : 'storeNodeAnnotations'
      });

      var colModelNodeAnnotations = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name",
        renderer : nameRenderer
      }, {
        header : "example (click to use query)",
        dataIndex : "values",
        renderer : readableExample
      }, {
        header : "url",
        width : 20,
        dataIndex : "id",
        renderer : annotationUrl
      } ]);

      var gridNodeAnnotations = new Ext.grid.GridPanel({
        ds : storeNodeAnnotations,
        cm : colModelNodeAnnotations,
        sm : new Ext.grid.RowSelectionModel({
          singleSelect : true,
          store : storeNodeAnnotations,
          listeners : {
            'rowselect' : queryToAnnisQL
          }
        }),
        loadMask : true,
        title : 'node annotations',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          rowOverCls : 'corpusbrowser-hover'
        },
        autoWidth : true,
        height : height
      });

      var storeEdgeAnnotations = new Ext.data.JsonStore({
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ],
        storeId : 'storeEdgeAnnotations'
      });

      var colModelEdgeAnnotation = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name",
        renderer : nameRenderer
      }, {
        header : "example (click to use query)",
        dataIndex : "values",
        renderer : edgeAnnotation
      }, {
        header : "url",
        width : 20,
        dataIndex : "id",
        renderer : annotationUrl
      } ]);

      var gridEdgeAnnotations = new Ext.grid.GridPanel({
        ds : storeEdgeAnnotations,
        cm : colModelEdgeAnnotation,
        sm : new Ext.grid.RowSelectionModel({
          singleSelect : true,
          store : storeEdgeAnnotations,
          listeners : {
            singleSelect : true,
            'rowselect' : queryToAnnisQL
          }
        }),
        loadMask : true,
        title : 'edge annotations',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          rowOverCls : 'corpusbrowser-hover'
        },
        autoWidth : true,
        height : height
      });

      var storeEdgeTypes = new Ext.data.JsonStore({
        remoteSort : false,
        fields : [ 'name', 'values', 'type', 'edge_name', 'subtype' ],
        storeId : 'storeEdgeTypes'
      });

      var colEdgeTypes = new Ext.grid.ColumnModel([ {
        header : "name",
        dataIndex : "name",
        renderer : nameRenderer
      }, {
        header : "example (click to use query)",
        dataIndex : "edge_name",
        renderer : edgeTypes
      }, {
        header : "url",
        width : 20,
        dataIndex : "id",
        renderer : annotationUrl
      } ]);

      var gridEdgeTypes = new Ext.grid.GridPanel({
        ds : storeEdgeTypes,
        cm : colEdgeTypes,
        sm : new Ext.grid.RowSelectionModel({
          singleSelect : true,
          store : storeEdgeTypes,
          listeners : {
            'rowselect' : queryToAnnisQL
          }
        }),
        loadMask : true,
        title : 'edge types',
        viewConfig : {
          forceFit : true,
          autoFill : true,
          rowOverCls : 'corpusbrowser-hover'
        },
        autoWidth : true,
        height : height
      });

      storeAttributes.setDefaultSort('name', 'asc');
      storeAttributes.on("load", function(store, records, options)
      {

        if (hasDominanceEdges(records))
        {
          var a = [];
          a[0] = '';
          var domEdgeType = {};
          domEdgeType.name = '(dominance)';
          domEdgeType.type = 'edge';
          domEdgeType.subtype = 'd';
          domEdgeType.edge_name = ' ';
          domEdgeType.values = a;
          var domEdgeTypeRecord = new storeEdgeTypes.recordType(domEdgeType,
              Ext.id());

          storeEdgeTypes.add(domEdgeTypeRecord);
        }

        // copy and filter for the several annotation-stores
        recordArray = store.getRange(0, store.getCount());
        for ( var i = 0; i < recordArray.length; i++)
        {
          storeEdgeAnnotations.add(recordArray[i].copy());
          storeEdgeTypes.add(recordArray[i].copy());
          storeNodeAnnotations.add(recordArray[i].copy());
        }

        var knownEdgeAnnotations = {};
        storeEdgeAnnotations.filterBy(function(record, id)
        {
          if (record.get("type") === 'edge')
          {
            var annoName = record.get('subtype') + '.'
                + record.get('edge_type') + '.' + record.get("name");
            if (knownEdgeAnnotations[annoName] !== true)
            {
              knownEdgeAnnotations[annoName] = true;
              return true;
            }
          }
          return false;
        });
        storeEdgeTypes.filterBy(edgeNames);

        storeNodeAnnotations.filter("type", "node", false, true);

      }); // end on load storeAttributes

      storeAttributes.load();

      rightPanel = new Ext.Panel({
        layout : 'accordion',
        title : 'available annotations',
        layoutConfig : {
          animate : true
        },
        items : [ gridNodeAnnotations, gridEdgeTypes, gridEdgeAnnotations ],
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
