
Ext.onReady(function(){

  var storeCorpusList = new Ext.data.JsonStore({
    url: conf_context + '/secure/CorpusList/NoFavorites',
    totalProperty: 'size',
    root: 'list',
    id: 'id',
    fields: [
      {name:'id', type:'int'},
      'name',
      {name:'textCount', type:'int'}, 
      {name:'tokenCount', type:'int'} 
    ],
    // turn on remote sorting
    remoteSort: false
  });
  storeCorpusList.setDefaultSort('name', 'asc');
    
  var corpusListSelectionModel = new Ext.grid.CheckboxSelectionModel(
  {
    singleSelect : false
  }
);
	
  var corpusListCm = new Ext.grid.ColumnModel([
    corpusListSelectionModel,
    {
      header: "Name",
      dataIndex: 'name',
      width: 170,
      align: 'left'
    },{
      header: "Texts",
      dataIndex: 'textCount',
      // width: 60,
      align: 'right'
    },{
      header: "Token",
      dataIndex: 'tokenCount',
      //width: 65,
      align: 'right'
    }
  ]);
  corpusListCm.defaultSortable = true;
    
  // create the Grid
  var corpusGrid = new Ext.grid.GridPanel({
    store: storeCorpusList,
    viewConfig: {
      forceFit:true,
      autoFill: true
    },
    loadMask: true,
    cm: corpusListCm,
    sm: corpusListSelectionModel,
    //width: 330,
    //height: 195,
    enableDragDrop:true,
    ddGroup: 'corpusList',
    stripeRows: true,
    title:'Available Corpora',
    header: false
  });
    
    
  var windowCorpusList = new Ext.Window({
    applyTo: 'windowCorpusList',
    hidden: true,
    title: 'All Available Corpora',
    id: 'windowCorpusList',
    closable:true,
    maximizable: true,
    width:800,
    height:605,
    //border:false,
    plain:true,
    layout: 'fit',
    closeAction: 'hide',
    items: [
      corpusGrid
    ]
  });
  windowCorpusList.render('windowCorpusList');
  storeCorpusList.load();
	 
  //extend dnd extender to allow dnd from corpus window
  var myDrop = new Ext.dd.DropTarget(corpusGrid.container, { 				   
    dropAllowed: 'x-dd-drop-ok', 
    ddGroup: 'corpusList', 
    notifyDrop: function(dd, e, data) { 
      var ds=data.grid.getStore();
      var dt = corpusGrid.getStore();
				
      dt.add(data.selections);
      var dtSortState = dt.getSortState();
      dt.sort(dtSortState.field,dtSortState.direction);
      var addedIds = [];
      for(var i=0;i<data.selections.length;i++) {
        ds.remove(data.selections[i]);		
        addedIds.push(data.selections[i].id);	
      }
      Ext.Ajax.request({
        url: conf_context + '/secure/CorpusList/Favorites',
        method: 'post',
        //success: someFn,
        //failure: otherFn,
        params: { remove: addedIds.join(",") }
      });			
      return true;
    }
    /*, 
	   notifyOver: function(dd, e, data) { 
	      //check if the row is allowed, return true or false 
	   } */
  });
});