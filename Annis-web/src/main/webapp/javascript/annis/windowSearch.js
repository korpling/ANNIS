var windowSearchFormWidth = 347;


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
      if(i>0) { 
        citation += ",";
      }
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
// end Citation

Ext.onReady(function()
{	

  Ext.Ajax.defaultHeaders = 
  {
    "Content-Type": "application/x-www-form-urlencoded; charset=utf-8"
  };

  var delayKeyTask = new Ext.util.DelayedTask();
  var keyDelay = 1000;
  var windowSearchFormWidthQueryBuilder = Ext.getBody().getViewSize().width-20;

  
  function updateStatus()
  {
    delayKeyTask.cancel();
    setSearchButtonDisabled(false);

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
      params: {
        'queryAnnisQL' : formPanelSearch.getComponent('queryAnnisQL').getValue()
      },
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
    formPanelSearch.getComponent('matchCount').setValue("Submitting query...");
			
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
				
    // submitting query to Server
    Ext.Ajax.request({
      url: conf_context + '/secure/SubmitQuery',
      method: 'GET',
      params: {
        'queryAnnisQL' : formPanelSearch.getComponent('queryAnnisQL').getValue(),
        'corpusIds': corpusIdString,
        'padLeft': formPanelSimpleSearch.getComponent('padLeft').getValue(),
        'padRight': formPanelSimpleSearch.getComponent('padRight').getValue()
      },
      success: function(response) 
      {
        isLoadingSearchResult = true;
        showCount();
        showWindowSearchResult();
      },
      failure: function() 
      {
        Ext.MessageBox.show({
          title: 'FATAL ERROR',
          msg: 'Unable to submit query. This may be caused by a connection problem.',
          icon: Ext.MessageBox.ERROR,
          buttons: Ext.MessageBox.OK
        });
        formPanelSearch.getComponent('matchCount').setValue("");
        setSearchButtonDisabled(false);
      },
      autoAbort: true,
      timeout: global_timeout
    });

    
  }
  // end getResult

  function showWeka()
  {
    if("" === formPanelSearch.getComponent('queryAnnisQL').getValue())
    {
      Ext.MessageBox.show({
        title: 'ERROR',
        msg: 'Empty query',
        icon: Ext.MessageBox.ERROR,
        buttons: Ext.MessageBox.OK
      });
      return;
    }

    if(!corpusListSelectionModel.hasSelection())
    {
      formPanelSearch.getComponent('matchCount').setValue("Please select a corpus!");
      return;
    }

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

    // open a new browser window/tab
    var url = conf_context + '/secure/WekaExporter?queryAnnisQL='
      + Url.encode(formPanelSearch.getComponent('queryAnnisQL').getValue())
      + '&corpusIds=' + corpusIdString;
    window.open(url, 'WekaExport');
  }
   		
  function setSearchButtonDisabled(disabled) 
  {
    Ext.ComponentMgr.get('btnSearch').setDisabled(disabled);
    Ext.ComponentMgr.get('btnWeka').setDisabled(disabled);
  }
  // end setSearchButtonDisabled

  /** A function that displays or updated the count for a search (after submitting it) */
  function showCount()
  {
    formPanelSearch.getComponent('matchCount').setValue("Getting match count...");
    // submitting query to Server
    Ext.Ajax.request({
      url: conf_context + '/secure/SearchResult',
      method: 'GET',
      params: {
        'count' : '1'
      },
      success: function(response)
      {
        formPanelSearch.getComponent('matchCount').setValue(response.responseText);
        formPanelSearch.getComponent('matchCount').getEl().
        frame('ff0000', 1, {
          duration:3
        });

        //the submit button
        setSearchButtonDisabled(false);
        if((response.responseText*1) <= 0 )
        {
          Ext.MessageBox.show({
            title: 'Info',
            msg: 'No search results',
            icon: Ext.MessageBox.INFO,
            buttons: Ext.MessageBox.OK
          });
        }
        else
        {
          
          if(!isLoadingSearchResult)
          {
            // update existing search result window totalCount
            var storeSearchResult = Ext.StoreMgr.get('storeSearchResult');
            
            storeSearchResult.reader.jsonData.totalCount  = (response.responseText*1);
            // load the slightly changed data (and don't ask the server again)
            storeSearchResult.loadData(storeSearchResult.reader.jsonData, false);
          }
        }
      },
      failure: function()
      {
        Ext.MessageBox.show({
          title: 'FATAL ERROR: count',
          msg: 'Unable to fetch match count. This may be caused by a timeout or connection problem.',
          icon: Ext.MessageBox.ERROR,
          buttons: Ext.MessageBox.OK
        });
        formPanelSearch.getComponent('matchCount').setValue("");
        setSearchButtonDisabled(false);
      },
      autoAbort: true,
      timeout: global_timeout
    });
  }

  /** A function that displays or updates the search result window */
  function showWindowSearchResult() 
  {
    var windowSearchResult = Ext.WindowMgr.get('windowSearchResult');
    var storeSearchResult = Ext.StoreMgr.get('storeSearchResult');

    var myLimit = 10;
    if("" !== resultLengthComboBox.getValue())
    {
      myLimit = (resultLengthComboBox.getValue()*1);
    }

    // adjust page size
    var gridSearchResult = windowSearchResult.getComponent('gridSearchResult');
    gridSearchResult.getTopToolbar().pageSize = myLimit;

    //open result window and update data store
    windowSearchResult.setTitle('Search Result - ' + formPanelSearch.getComponent('queryAnnisQL').getValue() + ' (' + formPanelSimpleSearch.getComponent('padLeft').getValue() + ', ' + formPanelSimpleSearch.getComponent('padRight').getValue() + ')');
    windowSearchResult.show();
    windowSearchResult.alignTo('windowSearchForm', 'tl', [windowSearchFormWidth + 5,0]);

    storeSearchResult.load({
      params:{
        start:0,
        limit:myLimit
      },
      callback: function(r, options, success)
      {
        if(!success)
        {
          Ext.MessageBox.show({
            title: 'FATAL ERROR: result',
            msg: 'Unable to fetch result. This may be caused by a timeout or connection problem.',
            icon: Ext.MessageBox.ERROR,
            buttons: Ext.MessageBox.OK
          });
          windowSearchResult.hide();
          setSearchButtonDisabled(false);
        }
      }
    });

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

  var resultLengthStore = new Ext.data.SimpleStore({
    fields: ['length'],
    data : [[1], [2], [5], [10], [15], [20], [25]]
  });

  var resultLengthComboBox = new Ext.form.ComboBox({
    store: resultLengthStore,
    name: 'resultLength',
    id: 'resultLength',
    fieldLabel: 'Results per page',
    displayField:'length',
    mode: 'local',
    triggerAction: 'all',
    value: '10',
    selectOnFocus:true,
    editable: false
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
    {
      name:'id',
      type:'int'
    },
    'name',
    {
      name:'textCount',
      type:'int'
    },

    {
      name:'tokenCount',
      type:'int'
    }
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
      align: 'left'
    },
    {
      header: "Texts",
      dataIndex: 'textCount',
      align: 'right'
    },
    {
      header: "Tokens",
      dataIndex: 'tokenCount',
      align: 'right'
    },
    {
      header: "",
      dataIndex:'id',
      align: 'right',
      renderer: renderCorpusInfo,
      width: 30
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
    width: 310,
    height: 240,
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
    title: 'Search',
    items: [ 
    padLeftComboBox,
    padRightComboBox,
    resultLengthComboBox
    ],
    buttons: [{
      id: 'btnSearch',
      text: 'Show Result',
      disabled: false,
      listeners: {
        click: getResult
      }
    }],
    buttonAlign:'center'
  });

  var formPanelStatistics = new Ext.FormPanel({
    id: 'formPanelStatistics',
    frame:true,
    title: 'Statistics',
    items: [],
    buttons: [{
      id: 'btnWeka',
      text: 'Weka Export',
      disabled: false,
      listeners: {
        click: showWeka
      }
    }],
    buttonAlign:'center'
  });

  var btnQueryBuilder = new Ext.Button({
    id: 'btnQueryBuilder',
    text: 'Show >>',
    fieldLabel: 'Query Builder',
    enableToggle: true,
    toggleHandler: function(button, state) {
      if(state)
      {
        button.setText('Hide <<');
        windowSearchForm.fireEvent('showQueryBuilder');
      }
      else
      {
        button.setText('Show >>');
        windowSearchForm.fireEvent('hideQueryBuilder');
      }
    }
  });
		
  var formPanelSearch = new Ext.FormPanel({
    id: 'formPanelSearch', 
    frame:true,
    title: 'AnnisQL',
    header: false,
    width: 330,
    height: 410,
    defaultType: 'textfield',
    monitorValid: true,
    items: [{
      id: 'queryAnnisQL',
      width: 200,
      height: 80,
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
    },
    btnQueryBuilder,
    {
      id: 'matchCount',
      width: 200,
      height: 40,
      xtype: 'textarea',
      fieldLabel: 'Result',
      name: 'matchCount',
      allowBlank:true,
      readOnly: true
    },
    corpusGrid
    ]
	           	
  });

		    
  var panelSearchModes = new Ext.TabPanel({
    width: 330,
    height: 160,
    activeTab: 0,
    items: [
      formPanelSimpleSearch,
      formPanelStatistics
    ]
  });

			
  var panelSearch = new Ext.Panel({
    region: 'west',
    width: 330,
    items: [formPanelSearch, panelSearchModes]
  });
	
  var queryBuilderURL = conf_context + '/queryBuilder.html';

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

  var isLoadingSearchResult = false;
  // add listener
  var storeSearchResult = Ext.StoreMgr.get('storeSearchResult');
  storeSearchResult.addListener("load", function(store, records, options)
  {
    isLoadingSearchResult = false;
  });

  // the main window
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
        params: { 
          add: addedIds.join(",")
        }
      });			
      return true;
    }
  //,
  //notifyOver: function(dd, e, data) {
  //   //check if the row is allowed, return true or false
  //}
  });
  
  // highlight tutorial
  Ext.get('tutorial').frame('ff0000', 5);
  
});

// end onReady

