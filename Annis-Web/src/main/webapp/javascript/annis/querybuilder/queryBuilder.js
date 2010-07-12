captionCreateEdge = 'Edge';
captionCancelEdge = 'Cancel';
captionClickHere = 'Dock';
captionNotAvailable = 'Dock';
captionClear = 'Clear';
captionAddField = 'Add';
captionDelete = 'X';

// helper //

Array.prototype.remove=function(s)
{
  for(i=0; i < this.length; i++)
  {
    if(this[i] == s)
    {
      this.splice(i,1);
    }
  }
};

// end helper //

//The field record type
var AnnisSearchField = Ext.data.Record.create([
  // the "name" below matches the tag name to read, except "availDate"
  // which is mapped to the tag "availability"
  {name: 'name', type: 'string'},
  {name: 'operator', type: 'string'},
  {name: 'value', type: 'string'}
]);
	
Ext.QuickTips.init();
		
//Field Store

var storeNodeAttributes = new Ext.data.JsonStore({
  url: conf_context + '/secure/AttributeList?noprefix',
  // turn on remote sorting
  remoteSort: false,
  fields: [ 'name', 'values' ]
});

storeNodeAttributes.setDefaultSort('name', 'asc');
storeNodeAttributes.load({params: {corpusIdList: '', type: 'node'}});
		
		
var storeFieldOperators = new Ext.data.SimpleStore({
  fields: ['operator'],
  data: [['='],['~'], ['!='], ['!~']]
});
		
var storeEdgeTypes = new Ext.data.SimpleStore({
  fields: ['operator', 'type'],
  data : 	[
    ['.'   , '.'				], 
    ['.*'  , '.*'				], 
    ['>'   , '>'					], 
    ['>*'  , '>*'				],
    ['>@l'  , '>@l'					],
    ['>@r'  , '>@r'					],
    ['$'   , '$'							],
    ['$*' , '$*'],
    ['->'   , '->'							],
    ['_=_' , '_=_'			],
    ['_i_' , '_i_'			],
    ['_l_' , '_l_'						],
    ['_r_' , '_r_'					],
    ['_o_', '_o_'					],
    ['_ol_', '_ol_'					],
    ['_or_', '_or_'					]

    //['@'   , '@'			]
  ]
});

// turn on validation errors beside the field globally
Ext.form.Field.prototype.msgTarget = 'side';
	
var nodeWindowList = [];
var edgeWindowList = [];
var createEdgeButtonList = [];
var edgeProperties = {};
var nodeProperties = {};
		
var edgeCount = 0;
var nodeCount = 0;
		
function isValidDestination(srcNodeId, dstNodeId) {
  if(srcNodeId == dstNodeId) {
    return false;
  }
  var state = true;
  Ext.each(edgeWindowList, function(edgeWindow) {
    if((edgeProperties[edgeWindow.getItemId()].srcNodeId == srcNodeId  && edgeProperties[edgeWindow.getItemId()].dstNodeId == dstNodeId)
        ||
        (edgeProperties[edgeWindow.getItemId()].srcNodeId == dstNodeId && edgeProperties[edgeWindow.getItemId()].dstNodeId == srcNodeId)) {
      state = false;
      return false;
    }
  });
  return state;
}

var edgeCreationSource = '';	
function resetCreateEdgeButtons() {
  edgeCreationSource = '';
  Ext.each(createEdgeButtonList, function(edgeButton) {
    var buttonNodeId = edgeButton.getItemId().replace('createEdgeButton', '');	
			
    if(nodeProperties[buttonNodeId].edgeOutIdList.length + nodeProperties[buttonNodeId].edgeInIdList.length  < nodeWindowList.length-1) {
      edgeButton.enable();
    } else {
      edgeButton.disable();
    }
    edgeButton.setText(captionCreateEdge);
    edgeButton.toggle(false);
  });
}

function getAnnisQLQuery() {
  var query = "";
  var nodeIdentityOperations = "";
  var nodeComponentMap = {};
			
			
  //creating node definitions
  var componentCount = 0;
  Ext.each(nodeWindowList, function(nodeWindow) {
    var id = nodeWindow.getItemId();
    var properties = nodeProperties[id];
				
    if(componentCount++ > 0) {
      query += " & ";
    }
				
    if(properties.store.getCount() > 0) {
      var nodeComponentCount = 0;
      properties.store.each(
      function(record) {
        if(nodeComponentCount++ > 0) {
          nodeIdentityOperations += ' & #' + componentCount  + ' _=_ #' + (componentCount+1);
          query += " & ";
          componentCount++;
        }
        var operator = record.get('operator').replace("~", "=");
        var quotes = (record.get('operator') == '='
          ||record.get('operator') == '!=' ) ? '"' : '/';
        var prefix = "";
        if(record.get('name').trim() !== "" || record.get('name') == 'word' || record.get('name') == 'text')
        {
          prefix = record.get('name') + operator;
        }
        else if(record.get('name').trim() == "" && operator == "!=")
        {
          prefix = 'tok' + record.get('name') + operator;
        }
        query += prefix + quotes + record.get('value') + quotes;
      }
    );
    } else {
      query += "node";
    }
    nodeComponentMap[id] = componentCount;
    hasComponent = true;
  });
  query += nodeIdentityOperations;
			
  //appending node relations
  Ext.each(edgeWindowList, function(edgeWindow) {
    var properties = edgeProperties[edgeWindow.getItemId()];
    query += " & ";
    query += '#' + nodeComponentMap[properties.srcNodeId] + ' ' + properties.operator + ' ' + '#' + nodeComponentMap[properties.dstNodeId];
  });
			
  return query;
}


function updateQueryField() {
  var fieldQueryAnnisQL = window.parent.Ext.ComponentMgr.get('queryAnnisQL');
  fieldQueryAnnisQL.setValue(getAnnisQLQuery());
  fieldQueryAnnisQL.fireEvent('keyup');
}


function getNewEdgePanel(edgeWindowId) {				
  var edgeTypeComboBox = new Ext.form.ComboBox({
    store: storeEdgeTypes,
    name: 'edgeType',
    width: 1,
    hideLabel: true,
    displayField:'type',
    valueField: 'operator',
    mode: 'local',
    triggerAction: 'all',
    value: '.',
    selectOnFocus:true,
    editable: true,
    listeners: {
      'select': {
        fn: function() { 
          edgeProperties[edgeWindowId].operator = this.getValue(); 
          //recompute query
          updateQueryField();
        }
      },
      'specialkey': {
        fn: function() {
          edgeProperties[edgeWindowId].operator = this.getRawValue(); 
          //recompute query
          updateQueryField();
        }
      },
      'blur': {
        fn: function() {
          edgeProperties[edgeWindowId].operator = this.getRawValue(); 
          //recompute query
          updateQueryField();
        }
      }
    }
  });
			
  edgeProperties[edgeWindowId].operator = edgeTypeComboBox.getValue();
			
  return new Ext.FormPanel({
    frame:true,
    labelAlign: 'top',
    layout:'fit',
    width: 1,
    items: [edgeTypeComboBox]
  });

}

function drawLine(srcId, dstId) {
  var srcWindow = Ext.WindowMgr.get(srcId);
  var dstWindow = Ext.WindowMgr.get(dstId);
			
  var srcNodeWindowPosition = srcWindow.getPosition();
  var dstNodeWindowPosition = dstWindow.getPosition();

  var srcNodeWindowSize = srcWindow.getSize();
  var dstNodeWindowSize = dstWindow.getSize();
			
  var srcPosition = [srcNodeWindowSize.width/2+srcNodeWindowPosition[0], srcNodeWindowSize.height/2+srcNodeWindowPosition[1]];
  var dstPosition = [dstNodeWindowSize.width/2+dstNodeWindowPosition[0], dstNodeWindowSize.height/2+dstNodeWindowPosition[1]];
			
  LineDrawer.drawLine(document.getElementById('canvas'), 
  srcPosition[0],
  srcPosition[1], 
  dstPosition[0],
  dstPosition[1]
  );
  // draw two arrows
  var v_x = dstPosition[0] - srcPosition[0];
  var v_y = dstPosition[1] - srcPosition[1];
  var origLength = Math.sqrt(Math.pow(v_x,2) + Math.pow(v_y,2)) ;
  
  var normV_x = v_x / origLength;
  var normV_y = v_y / origLength;
  
  var arrowSize = 20;
  var origDir = Math.atan2(normV_y, normV_x);

  var pos1_x = (2.1*origLength/3)*normV_x + srcPosition[0];
  var pos1_y = (2.1*origLength/3)*normV_y + srcPosition[1];
  
  var canvas = document.getElementById('canvas');
  LineDrawer.drawArrow(canvas, pos1_x, pos1_y, origDir, arrowSize);

  var pos2_x = ((1*origLength)/3)*normV_x + srcPosition[0];
  var pos2_y = ((1*origLength)/3)*normV_y + srcPosition[1];
  LineDrawer.drawArrow(canvas, pos2_x, pos2_y, origDir, arrowSize);

}

function determineMiddlePoint(srcId, dstId) {
  var srcWindow = Ext.WindowMgr.get(srcId);
  var dstWindow = Ext.WindowMgr.get(dstId);
				
  var srcNodeWindowPosition = srcWindow.getPosition();
  var dstNodeWindowPosition = dstWindow.getPosition();
			
  var srcNodeWindowSize = srcWindow.getSize();
  var dstNodeWindowSize = dstWindow.getSize();
			
  return [(srcNodeWindowPosition[0]+srcNodeWindowSize.width/2+dstNodeWindowPosition[0]+srcNodeWindowSize.width/2)/2, (srcNodeWindowPosition[1]+srcNodeWindowSize.height/2+dstNodeWindowPosition[1]+dstNodeWindowSize.height/2)/2];
}

function alignAllEdgeWindows() {
  LineDrawer.clear(document.getElementById('canvas'));
			
  Ext.each(edgeWindowList, function(edgeWindow) {
    var edgeWindowPosition = edgeWindow.getPosition();
    var middlePoint = determineMiddlePoint(edgeProperties[edgeWindow.getItemId()].srcNodeId, edgeProperties[edgeWindow.getItemId()].dstNodeId);
    edgeWindow.setPosition(middlePoint[0] - edgeWindow.getSize().width/2, middlePoint[1]-edgeWindow.getSize().height/2);
				
    drawLine(edgeProperties[edgeWindow.getItemId()].srcNodeId, edgeProperties[edgeWindow.getItemId()].dstNodeId);
  });
}

function removeEdge(id) {
  var edgeWindow = Ext.WindowMgr.get(id);
  edgeWindow.fireEvent('close');
  edgeWindow.destroy();
}


function createEdgeWindow(srcWindowId, dstWindowId) {
  edgeCount++;
  var edgeId = 'edge' + edgeCount;
  edgeProperties[edgeId] = {srcNodeId: srcWindowId, dstNodeId: dstWindowId};
  var edgeWindow = new Ext.Window({
    title: 'Edge Specification ' + edgeCount,
    id: edgeId,
    maximizable: false,
    width:80,
    height:82,
    minWidth:80,
    minHeight:82,
    closable: false,
    draggable : false,
    resizable : true,
    headerAsText : false,
    border    : false,
    //border:false,
    plain:true,
    layout: 'fit',
    listeners: {
      'close' : {
        fn: function() {
          //Cleanup related lists
          var id = this.getItemId();
          var properties = edgeProperties[id];
          nodeProperties[properties.srcNodeId].edgeOutIdList.remove(id);
          nodeProperties[properties.dstNodeId].edgeInIdList.remove(id);
										
											
          edgeProperties[this.getItemId()] = null;
          edgeWindowList.remove(this);
											
          //Reset create edge button states
          resetCreateEdgeButtons();
											
          //recompute query
          updateQueryField();
        }
      }
    },
    items: [getNewEdgePanel(edgeId)],
    tbar: [{
        text: captionDelete,
        listeners: {
          click: function() { 
            removeEdge(edgeId);
            //Redraw edges
            alignAllEdgeWindows();
          }
        }
      }
    ]
  });
			
			
  var outList = nodeProperties[srcWindowId].edgeOutIdList;
  outList.push(edgeId);
  nodeProperties[srcWindowId].edgeOutIdList = outList;
			
  var inList = nodeProperties[dstWindowId].edgeInIdList;
  inList.push(edgeId);
  nodeProperties[dstWindowId].edgeInIdList = inList;
			
  edgeWindow.show();			
  edgeWindowList.push(edgeWindow);
			
  //aligning this edge window to its related nodes
  alignAllEdgeWindows();
			
  //recompute query
  updateQueryField();
}
		
function getNewNodePanel(nodeWindowId) {
  var buttonId = nodeWindowId + 'createEdgeButton';
  var button = new Ext.Button({
    text: captionCreateEdge,
    id: buttonId,
    enableToogle: true,
    disabled: createEdgeButtonList.length < 1,
    listeners: {
      'click': {
        fn: function() { 
          var nodeId = this.id.replace('createEdgeButton', '');
          if(edgeCreationSource === '') {
            for(var i=0; i<createEdgeButtonList.length; i++) {
              var createEdgeButton = createEdgeButtonList[i];
              var buttonNodeId = createEdgeButton.getItemId().replace('createEdgeButton', '');
																										
              if(isValidDestination(nodeId, buttonNodeId)) {
                //this node window is still available
                createEdgeButton.setText(captionClickHere);
              } else {
                createEdgeButton.disable();
                createEdgeButton.setText(captionNotAvailable);
              }
              //start edgeCreation
              edgeCreationSource = nodeId;
              this.setText(captionCancelEdge);
              this.toggle(true);
              this.enable();
            }
            //toogle this button
          } else {
            if(!this.pressed) {
              //create edge to this point
              createEdgeWindow(edgeCreationSource, nodeId);
            }
            //Reset all button states
            resetCreateEdgeButtons();
          }
        }
      }
					
					
    },
    tooltip: {
      text:'To create a new edge between two nodes click this button first. Then define a destination node by clicking its "Click Here" button.<br>You can cancel the action by clicking this button ("Cancel Edge") again.', 
      title:captionCreateEdge,
      autoHide:true
						
    }
  });
  createEdgeButtonList.push(button);
			
  var store = new Ext.data.SimpleStore({
    fields: ['name', 'operator', 'value'],
    data: []
  });
  nodeProperties[nodeWindowId].store = store;
  var cm = new Ext.grid.ColumnModel([
    {
      id:'name',
      header: "Field",
      dataIndex: 'name',
      //width: 124,
      editor: new Ext.form.ComboBox({
        store: storeNodeAttributes,
        displayField: 'name',
        typeAhead: true,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true,
        allowBlank: true
      })
			
			
    },
    {
      id:'operator',
      header: 'op',
      dataIndex: 'operator',
      width: 45,
      editor: new Ext.form.ComboBox({
        store: storeFieldOperators,
        displayField: 'operator',
        mode: 'local',
        triggerAction: 'all',
        editable: false,
        selectOnFocus:true
      })
    },
    {
      id:'value',
      header: "Value",
      dataIndex: 'value',
      //width: 90,
      editor: new Ext.form.TextField({
        allowBlank: true
      })
    }
  ]);
			
  var grid = new Ext.grid.EditorGridPanel({
    store: store,
    cm: cm,
    clicksToEdit: 1,
    viewConfig: {
      forceFit:true,
      autoFill: true
    },
    //width: 265,
    tbar: [	button,
      {
        text: captionAddField,
        handler : function(){
          var f = new AnnisSearchField({
            name: '',
            operator: '=',
            value: ''
          });
          grid.stopEditing();
          store.insert(0, f);
          grid.startEditing(0, 0);
        }
      },
      {
        text: captionClear,
        handler : function(){
          grid.stopEditing();
          try {		
            store.removeAll();
          } catch (e) {
            alert(e);
          }
          //store.remove(grid.getSelectionModel().getSelected());
        }
      },
      {
        text: captionDelete,
        handler : function(){
          var nodeWindow = Ext.WindowMgr.get(nodeWindowId);
          nodeWindow.fireEvent('close');
          nodeWindow.destroy();
        }
      }
    ],
    listeners : {
      'afteredit' : {
        fn: function() {
          //recompute query
          updateQueryField();
        }
      }
    }
  });
  return grid;
}
		
		
function createNodeWindow() {
  nodeCount++;
  var nodeId = 'node' + nodeCount;
  nodeProperties[nodeId] = {edgeInIdList : [], edgeOutIdList: [], operator: ''};
  var nodeWindow = new Ext.Window({
    title: 'Node Specification ' + nodeCount,
    id: nodeId,
    //closable:false,
    maximizable: false,
    //headerAsText : false,
    border    : false,
    closable: false,
    //draggable : false,
    resizable : true,
    headerAsText : false,
    width:150,
    height:150,
    plain:true,
    layout: 'fit',
    autoScroll: true,
    items: [getNewNodePanel(nodeId)],
    listeners: {
      'move' : {
								
        fn: alignAllEdgeWindows
      },
      'resize' : {

        fn: alignAllEdgeWindows
      },
      'close' : {
        fn: function() {
          //Cleanup related edges and lists
											
          var itemId = this.getItemId();
          //Removing this node from node list		
          nodeWindowList.remove(this);
          
          var button = null;
          Ext.each(createEdgeButtonList, function(edgeButton) {
            if(edgeButton.getItemId() == (itemId + 'createEdgeButton')) {
              button = edgeButton;
              return false;
            }
          });
          createEdgeButtonList.remove(button);
																	
          //Remove all associated edges
          try { 
            Ext.each(nodeProperties[itemId].edgeOutIdList, removeEdge);
            Ext.each(nodeProperties[itemId].edgeInIdList, removeEdge);
											
            nodeProperties[itemId] = null;
          } catch (e) {
            //ignore
          }
											
          //Disabling last create edge button
          if(createEdgeButtonList.length == 1) {
            createEdgeButtonList[0].disable();
          }
          //Reset create edge button states
          resetCreateEdgeButtons();
											
          //Redraw all edges
          alignAllEdgeWindows();
											
          //recompute query
          updateQueryField();
        }
      }
						
    }
  });
  nodeWindow.show();
			
  try {
    var lastWindow = nodeWindowList[nodeWindowList.length-1];
    if(lastWindow !== null)
    {
      var id = lastWindow.getItemId();
      nodeWindow.alignTo(id, 'tr', [95, 0]);
    }
    else
    {
      nodeWindow.setPosition(5, 325);
    }
  } catch (e) {
    nodeWindow.setPosition(5, 325);
  }
  nodeWindowList.push(nodeWindow);
			
  //recompute query
  updateQueryField();
			
  //activate all create node buttons if there are more than one node windows
  if(createEdgeButtonList.length > 1) {
    //Reset create edge button states
    resetCreateEdgeButtons();
  } else {
    createEdgeButtonList[0].disable();
  }
}
		
		
