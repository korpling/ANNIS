var Citation = {
  generate : function() {
    var formPanelSearch = Ext.ComponentMgr.get('formPanelSearch');
    var formPanelSimpleSearch = Ext.ComponentMgr.get('formPanelSimpleSearch');
    var selections = Ext.ComponentMgr.get('gridPanelCorpus').selModel.getSelections();
		
    var contextRaw = conf_context.substr(1);
    var reg = new RegExp(contextRaw + '.*', 'i');
    var citation = window.location.href.replace(reg, contextRaw + '/Cite/');
    citation += "AQL(" + Url.encode(formPanelSearch.getComponent('queryAnnisQL').getValue()) + ")";
    citation += ",CIDS(";
    for(i=0;i<selections.length;i++) {
      if(i>0) { citation += ","; }
      citation += selections[i].id;
    }
    citation += ")";
    citation += ",CLEFT(" + formPanelSimpleSearch.getComponent('padLeft').getValue() + ")";
    citation += ",CRIGHT(" + formPanelSimpleSearch.getComponent('padRight').getValue() + ")";
    return citation;
  },

  setFromCookie : function() {
    //finally we evaluate the citation cookie value and set up the search window accordingly
    var cookies = document.cookie;
    //grep citation value from cookie
    var citation = (cookies.indexOf("citation=") > -1) ? cookies.replace(/.*?citation="(.*?)".*/,"$1") : "";
    if("" !== citation) {
      //parse citation string for all values and set them up
      var formPanelSearch = Ext.ComponentMgr.get('formPanelSearch');
      var formPanelSimpleSearch = Ext.ComponentMgr.get('formPanelSimpleSearch');
      var store = Ext.ComponentMgr.get('gridPanelCorpus').getStore();
      var selectionModel = Ext.ComponentMgr.get('gridPanelCorpus').selModel;
			
      Ext.each(citation.split('),'), function(item){
        item=item.replace(')', '');
        var pair = item.split('(');
        var key = pair[0];
        var value = pair[1];
					
        if("AQL" == key) {
          formPanelSearch.getComponent('queryAnnisQL').setValue(Url.decode(value.replace('"', '')));
        } else if("CIDS" == key) {
          var cids = value.split(',');
          for(i=0;i<cids.length;i++) {
            cids[i] = store.getById(cids[i]);
          }
          selectionModel.selectRecords(cids, false);
        } else if("CLEFT" == key) {
          formPanelSimpleSearch.getComponent('padLeft').setValue(value);
        } else if("CRIGHT" == key) {
          formPanelSimpleSearch.getComponent('padRight').setValue(value);
        }
      });
    }
    //todo delete cookie
    document.cookie = "citation=; path=/;-1";
  }
};

Ext.onReady(function()
{	

  Ext.Ajax.defaultHeaders = 
    { "Content-Type": "application/x-www-form-urlencoded; charset=utf-8"};

  var delayKeyTask = new Ext.util.DelayedTask();
  var windowSearchFormWidth = 347;
  var windowSearchFormWidthQueryBuilder = 1000;
  var keyDelay = 1000;

  
  function updateStatus()
  {
    delayKeyTask.cancel();
    
    if(!corpusListSelectionModel.hasSelection()) 
    {
      formPanelSearch.getComponent('matchCount').setValue("Please select a corpus!");
      return;
    }

    // just a dummy for the good old days (or for the newer ones)
    formPanelSearch.getComponent('matchCount').setValue("");


    Ext.Ajax.request({
      url: conf_context + '/secure/ValidateQuery',
      method: 'GET',
      params: {'queryAnnisQL' : formPanelSearch.getComponent('queryAnnisQL').getValue()},
      success: function(response) {
        formPanelSearch.getComponent('matchCount').setValue(response.responseText);
      },
      failure: function() {
        formPanelSearch.getComponent('matchCount').setValue("FATAL ERROR: Unable to validate query.");
      },
      autoAbort: true,
      timeout: 10000
    });

  } // end updateStatus
	
  
  function getResult()
  {
    delayKeyTask.cancel();
    //run only if at least one corpus is selected
    if("" === formPanelSearch.getComponent('queryAnnisQL').getValue()) 
    {
      formPanelSearch.getComponent('matchCount').setValue("0");
      return;
    }
			
    if(!corpusListSelectionModel.hasSelection()) 
    {
      formPanelSearch.getComponent('matchCount').setValue("Please select a corpus!");
      return;
    }
		
    // no double requests...
    setSearchButtonDisabled(true);

    //set info box to something neutral
    formPanelSearch.getComponent('matchCount').setValue("Calculating...");
			
    //Gather selections
    var selections = corpusListSelectionModel.getSelections();
    var corpusIdString = "";
    for(var i=0;i<selections.length;i++) 
    {
      if(i !== 0) 
      {
        corpusIdString += ",";
      }
      corpusIdString += selections[i].id;
    }
				
    //Retrieving matchCount from Server
    Ext.Ajax.request({
      url: conf_context + '/secure/SubmitQuery',
      method: 'GET',
      params: {'queryAnnisQL' : formPanelSearch.getComponent('queryAnnisQL').getValue(), 'corpusIds': corpusIdString, 'padLeft': formPanelSimpleSearch.getComponent('padLeft').getValue(), 'padRight': formPanelSimpleSearch.getComponent('padRight').getValue()},
      success: function(response) {
        formPanelSearch.getComponent('matchCount').setValue(response.responseText);
        //the submit button
        setSearchButtonDisabled(false);
        var windowSearchResult =  Ext.WindowMgr.get('windowSearchResult');
        windowSearchResult.hide();
        if((response.responseText*1) > 0 )
        {
          showWindowSearchResult();
        }
        else
        {
          Ext.MessageBox.show({
            title: 'Info',
            msg: 'No search results',
            icon: Ext.MessageBox.INFO,
            buttons: Ext.MessageBox.OK
          });
        }
      },
      failure: function() {
        formPanelSearch.getComponent('matchCount').setValue("FATAL ERROR: Unable to fetch match Count.");
        setSearchButtonDisabled(false);
      },
      autoAbort: true,
      timeout: 60000
    });
  }
  // end getResult
   		
  function setSearchButtonDisabled(disabled) 
  {
    Ext.ComponentMgr.get('btnSimpleSearch').setDisabled(disabled);
    Ext.ComponentMgr.get('btnQueryBuilder').setDisabled(disabled);
  }
  // end setSearchButtonDisabled
   		
  //A function that displays or updates the search result window
  function showWindowSearchResult() 
  {
    var windowSearchResult = Ext.WindowMgr.get('windowSearchResult');
    var storeSearchResult = Ext.StoreMgr.get('storeSearchResult');

    //open result window and update data store
    windowSearchResult.setTitle('Search Result - ' + formPanelSearch.getComponent('queryAnnisQL').getValue() + ' (' + formPanelSimpleSearch.getComponent('padLeft').getValue() + ', ' + formPanelSimpleSearch.getComponent('padRight').getValue() + ')');
    windowSearchResult.show();
    windowSearchResult.alignTo('windowSearchForm', 'tl', [windowSearchFormWidth + 5,0]);
    storeSearchResult.load({params:{start:0, limit:25}});

  }
  // end showWindowSearchResult


  //The pad store ;)
  var padStore = new Ext.data.SimpleStore({
    fields: ['pad'],
    data : [[0], [1], [2], [5], [10]]
  });
	    
  var padLeftComboBox = new Ext.form.ComboBox({
    store: padStore,
    name: 'padLeft',
    id: 'padLeft',
    fieldLabel: 'Context Left',
    displayField:'pad',
    mode: 'local',
    triggerAction: 'all',
    value: '5',
    selectOnFocus:true,
    editable: false,
    listeners: {
      'select': {
        fn: updateStatus,
        scope: this
      }
    }
  });
	    
  var padRightComboBox = new Ext.form.ComboBox({
    store: padStore,
    name: 'padRight',
    id: 'padRight',
    fieldLabel: 'Context Right',
    displayField:'pad',
    mode: 'local',
    triggerAction: 'all',
    value: '5',
    selectOnFocus:true,
    editable: false,
    listeners: {
      'select': {
        fn: updateStatus,
        scope: this
      }
    }
  });

  //The Search Window

  Ext.override(Ext.form.TextArea, 
  {
    fireKey : function(e) 
    {
      if(((Ext.isIE && e.type == 'keydown') || e.type == 'keypress') && e.isSpecialKey()) 
      {
        this.fireEvent('specialkey', this, e);
      }
      else 
      {
        this.fireEvent(e.type, this, e);
      }
    },
    initEvents : function()
    {
      //                this.el.on(Ext.isIE ? "keydown" : "keypress", this.fireKey,  this);
      this.el.on("focus", this.onFocus,  this);
      this.el.on("blur", this.onBlur,  this);
      this.el.on("keydown", this.fireKey, this);
      this.el.on("keypress", this.fireKey, this);
      this.el.on("keyup", this.fireKey, this);
        
      // reference to original value for reset
      this.originalValue = this.getValue();
    }
  });
		
  Ext.QuickTips.init();

  // turn on validation errors beside the field globally
  Ext.form.Field.prototype.msgTarget = 'side';	
		
  //Corpus Grid View
  var storeFavoriteCorpusList = new Ext.data.JsonStore({
    url: conf_context + '/secure/CorpusList/Favorites',
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
    remoteSort: false,
    listeners: {
      'load': {
        fn: Citation.setFromCookie
      }
    }
  });
  
  storeFavoriteCorpusList.setDefaultSort('name', 'asc');
		
  var corpusListSelectionModel = new Ext.grid.CheckboxSelectionModel(
  {
    singleSelect : false,
    listeners: {
      'rowselect': {
        fn: updateStatus,
        scope: this
      },
      'rowdeselect': {
        fn: updateStatus,
        scope: this
      }    
    }
  }
);
		
  var corpusListCm = new Ext.grid.ColumnModel([
    corpusListSelectionModel, 
    {
      header: "Name",
      dataIndex: 'name',
      width: 170,
      align: 'left'
    },
    {
      header: "Texts",
      dataIndex: 'textCount',
      // width: 60,
      align: 'right'
    },
    {
      header: "Token",
      dataIndex: 'tokenCount',
      //width: 65,
      align: 'right'
    }
  ]);
  corpusListCm.defaultSortable = true;
		
  // create the Grid
  var corpusGrid = new Ext.grid.GridPanel({
    id: 'gridPanelCorpus',
    enableDragDrop:true,
    ddGroup: 'corpusList',
    store: storeFavoriteCorpusList,
    viewConfig: {
      forceFit:true,
      autoFill: true
    },
    loadMask: true,
    cm: corpusListCm,
    sm: corpusListSelectionModel,
    //width: 330,
    height: 195,
    stripeRows: true,
    title:'Available Corpora',
    header: false,
    tbar: new Ext.Toolbar({
      items: [
        new Ext.Toolbar.Button({
          text: 'More Corpora',
          handler: function() {
            var windowCorpusList = Ext.WindowMgr.get('windowCorpusList');
            windowCorpusList.show();
            windowCorpusList.alignTo('windowSearchForm', 'tr', [10,0]);
          },
          disabled: false,
          tooltip: {
            text:'Click here to open the corpus selection Window.', 
            title:'More Corpora',
            autoHide:true
															
          }
        })
      ]
    })
  });
		
  var formPanelSimpleSearch = new Ext.FormPanel({
    id: 'formPanelSimpleSearch',
    frame:true,
    title: 'Simple Search',
    height: 200,
    items: [ 
      padLeftComboBox,
      padRightComboBox
    ],
    buttons: [{
        id: 'btnSimpleSearch',
        text: 'Show Result',
        disabled: false,
        listeners: {
          click: getResult
        }
      }]
  });
		
  var formPanelSearch = new Ext.FormPanel({
    id: 'formPanelSearch', 
    frame:true,
    title: 'AnnisQL',
    header: false,
    width: 330,
    height: 300,
    defaultType: 'textfield',
    monitorValid: true,
    items: [{
        id: 'queryAnnisQL',
        width: 200,
        fieldLabel: 'AnnisQL',
        name: 'queryAnnisQL',
        allowBlank:true,
        xtype: 'textarea',
        listeners: {
          'keyup': {
            fn: function() {
              formPanelSearch.getComponent('matchCount').setValue("Delay...");
              delayKeyTask.delay(keyDelay, updateStatus);
            },
            scope: this
          }
        }
      },{
        id: 'matchCount',
        width: 200,
        fieldLabel: 'Match Count',
        name: 'matchCount',
        allowBlank:true,
        readOnly: true
      },
      corpusGrid
    ]
	           	
  });

  var formPanelQueryBuilder = new Ext.FormPanel({
    title : 'Query Builder',
    frame: true,
    listeners: {
      'activate': {
        fn: function() {
          windowSearchForm.fireEvent('showQueryBuilder');
        },
        scope: this
      },
      'deactivate': {
        fn: function() {
          windowSearchForm.fireEvent('hideQueryBuilder');
        },
        scope: this
      }
    },
    items: [{
        id: 'blaa',
        width: 200,
        fieldLabel: 'Match Count',
        name: 'matchCount',
        allowBlank:true,
        readOnly: true
      }],
    buttons: [{
        id: 'btnQueryBuilder',
        text: 'Show Result',
        disabled: false,
        listeners: {
          click: getResult
        }
      }]
  });
		    
  var panelSearchModes = new Ext.TabPanel({
    width: 330,
    height: 270,
    activeTab: 0,
    items: [
      formPanelSimpleSearch,
      formPanelQueryBuilder,
      {
        title: 'Statistics',
        frame:true
      }]
  });

			
  var panelSearch = new Ext.Panel({
    region: 'west',
    width: 330,
    items: [formPanelSearch, panelSearchModes]
  });
	
  var queryBuilderURL = conf_context + '/queryBuilder.jsp';

  var panelQueryBuilder = new Ext.Panel({
    id: 'panelQueryBuilder',
    region: 'center',
    items : {
      html: '<iframe id="iframeQueryBuilder" name="iframeQueryBuilder" src="' + queryBuilderURL + '" width="100%" height="100%" frameborder="0"></iframe>'
    },
    tbar: new Ext.Toolbar({
      items: [
        new Ext.Toolbar.Button({
          text: 'Create Node',
          handler: function() {
            window.frames.iframeQueryBuilder.createNodeWindow();
          },
          tooltip: {
            text:'Click here to add a new node specification window.', 
            title:'Create Node',
            autoHide:true
															
          }
        })
      ]
    })
  });

  var windowSearchForm = new Ext.Window({
    title: 'Search Form',
    id: 'windowSearchForm',
    closable:false,
    collapsible: false,
    maximizable: true,
    resizable: false,
    width: windowSearchFormWidth,
    height:605,
    //border:false,
    plain:true,
    closeAction: 'hide',
    layout: 'border',
    items: [
      panelSearch,
      panelQueryBuilder
    ],
    listeners: {
      'showQueryBuilder': {
        fn: function() {
          windowSearchForm.setWidth(windowSearchFormWidthQueryBuilder);
          var nodeMaximize = Ext.DomQuery.selectNode('div[class~=x-tool-maximize]', document.getElementById(windowSearchForm.id));
          nodeMaximize.style.visibility = 'visible';
        },
        scope: this
      },
      'hideQueryBuilder': {
        fn: function() {
          windowSearchForm.setWidth(windowSearchFormWidth);
          var nodeMaximize = Ext.DomQuery.selectNode('div[class~=x-tool-maximize]', document.getElementById(windowSearchForm.id));
          nodeMaximize.style.visibility = 'hidden';
        },
        scope: this
      }
    }
  });
		 
  windowSearchForm.setPosition(0, 30);
  windowSearchForm.show();
		 
  //hide the maximize button
  windowSearchForm.fireEvent('hideQueryBuilder');
		 
  //loading corpus list
  storeFavoriteCorpusList.load();
		 
		 
  //extend dnd extender to allow dnd from corpus window
  var myDrop = new Ext.dd.DropTarget(corpusGrid.container, { 				   
    dropAllowed: 'x-dd-drop-ok', 
    ddGroup: 'corpusList', 
    notifyDrop: function(dd, e, data) { 
      //
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
        params: { add: addedIds.join(",") }
      });			
      return true;
    }
    //,
    //notifyOver: function(dd, e, data) { 
    //   //check if the row is allowed, return true or false 
    //}
  });
  
  // we may have serious problems when using not Firefox
  if(Ext.isIE)
  { 
    Ext.MessageBox.show({
      title: 'WARNING - Unsupported Browser',
      msg: 'You are not using Firefox as your webbrowser. This may cause ' +
        'serious problems when using Annis&sup2;. If you don\'t have Firefox yet, ' +
        'you can download it from <a href="http://mozilla.com/firefox">http://mozilla.com/firefox</a>.<br /><br />' +
        'Click on "OK" to proceed, but Annis&sup2; will probably not work as expected.',
      icon: Ext.MessageBox.WARNING,
      buttons: Ext.MessageBox.OK
    });
  }
  
});

// end onReady

