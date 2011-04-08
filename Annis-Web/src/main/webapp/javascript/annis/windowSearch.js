var windowSearchFormWidth = 347;
var lastQuery = '';
var lastStatus = '';

function corpusStringListFromSelection(selections)
{
  var result = "";
  for(var i=0;i<selections.length;i++)
  {
    if(i !== 0)
    {
      result += ",";
    }
    result += selections[i].id;
  }
  return result;
}

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
      citation += selections[i].json.name;
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
        } else if("CIDS" == key)
        {
          var cids = value.split(',');
          var selection = new Array();
          for(i=0;i<cids.length;i++)
          {
            var index = store.findExact('name',cids[i]);
            selection[i] = store.getAt(index);
          }
          selectionModel.selectRecords(selection, false);
        } 
        else if("CLEFT" == key)
        {
          formPanelSimpleSearch.getComponent('padLeft').setValue(value);
        } 
        else if("CRIGHT" == key)
        {
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

    if(lastQuery === formPanelSearch.getComponent('queryAnnisQL').getValue())
    {
      formPanelSearch.getComponent('matchCount').setValue(lastStatus);
    }
    else
    {
      lastQuery = formPanelSearch.getComponent('queryAnnisQL').getValue();
      
      Ext.Ajax.request({
        url: conf_context + '/secure/ValidateQuery',
        method: 'GET',
        params: {
          'queryAnnisQL' : formPanelSearch.getComponent('queryAnnisQL').getValue()
        },
        success: function(response) {
          formPanelSearch.getComponent('matchCount').setValue(response.responseText);
          lastStatus = formPanelSearch.getComponent('matchCount').getValue();
        },
        failure: function() {
          formPanelSearch.getComponent('matchCount').setValue("FATAL ERROR: Unable to validate query.");
          lastStatus = formPanelSearch.getComponent('matchCount').getValue();
        },
        autoAbort: false,
        timeout: 10000
      });

    }

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
    var corpusIdString = corpusStringListFromSelection(selections);

    lastQuery = formPanelSearch.getComponent('queryAnnisQL').getValue();
				
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
        lastStatus="";
        setSearchButtonDisabled(false);
      },
      autoAbort: true,
      timeout: global_timeout
    });

    
  }
  // end getResult

  function doExport()
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
    var corpusIdString = corpusStringListFromSelection(selections);

    var exporterSelection = formPanelExporter.getComponent("exportSelection").getValue();


    // open a new browser window/tab
    var url = conf_context + '/secure/' + exporterSelection + '?queryAnnisQL='
      + encodeURIComponent(formPanelSearch.getComponent('queryAnnisQL').getValue())
      + '&corpusIds=' + corpusIdString
      + '&padLeft=' + formPanelExporter.getComponent('padLeftExport').getValue()
      + '&padRight=' + formPanelExporter.getComponent('padRightExport').getValue();

    var additionalParams = formPanelExporter.getComponent('additionalParamsExport').getValue();
    if(additionalParams !== "")
    {
      url += '&' + additionalParams;
    }    
    window.open(url, 'Export');
  }
   		
  function setSearchButtonDisabled(disabled) 
  {
    Ext.ComponentMgr.get('btnSearch').setDisabled(disabled);
    Ext.ComponentMgr.get('btnExport').setDisabled(disabled);
  }
  // end setSearchButtonDisabled

  /** A function that displays or updated the count for a search (after submitting it) */
  function showCount()
  {
    lastQuery = formPanelSearch.getComponent('queryAnnisQL').getValue();
    lastStatus = formPanelSearch.getComponent('matchCount').getValue();
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
        lastStatus = formPanelSearch.getComponent('matchCount').getValue();
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
      failure: function(response)
      {
        formPanelSearch.getComponent('matchCount').setValue(response.responseText);
        lastStatus = formPanelSearch.getComponent('matchCount').getValue();
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
    data : search_context
  });
	    
  var padLeftComboBox = new Ext.form.ComboBox({
    store: padStore,
    name: 'padLeft',
    id: 'padLeft',
    fieldLabel: 'Context Left',
    displayField:'pad',
    mode: 'local',
    triggerAction: 'all',
    value: '' + search_context_default,
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
    value: '' + search_context_default,
    selectOnFocus:true,
    editable: false,
    listeners: {
      'select': {
        fn: updateStatus,
        scope: this
      }
    }
  });

  var padLeftComboBoxExport = new Ext.form.ComboBox({
    store: padStore,
    name: 'padLeftExport',
    id: 'padLeftExport',
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

  var padRightComboBoxExport = new Ext.form.ComboBox({
    store: padStore,
    name: 'padRightExport',
    id: 'padRightExport',
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

var additionalParamsExport = new Ext.form.TextField({
    name: 'additionalParamsExport',
    id: 'additionalParamsExport',
    fieldLabel: 'Parameters',
    value: '',
    selectOnFocus:true,
    listeners: {
      'select': {
        fn: updateStatus,
        scope: this
      }
    }
  });

var exportStore = new Ext.data.SimpleStore({
    fields: ['type'],
    data : [['SimpleTextExporter'], ['TextExporter'], ['WekaExporter'], ['GridExporter']]
  });
var exportSelection = new Ext.form.ComboBox({
    store: exportStore,
    displayField:'type',
    mode: 'local',
    name: 'exportSelection',
    id: 'exportSelection',
    fieldLabel: 'Exporter',
    triggerAction: 'all',
    value: 'WekaExporter',
    selectOnFocus:true,
    editable: false
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
      id: "name",
      header: "Name",
      dataIndex: 'name',
      align: 'left'
    },
    {
      id: "texts",
      header: "Texts",
      dataIndex: 'textCount',
      align: 'right',
      width: 50
    },
    {
      id: "tokens",
      header: "Tokens",
      dataIndex: 'tokenCount',
      align: 'right',
      width: 50
    },
    {
      id: "id",
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
    autoExpandColumn: 'name',
    enableDragDrop:true,
    ddGroup: 'corpusList',
    store: storeFavoriteCorpusList,
    viewConfig: {
      //forceFit:true
      autoFill: true
    },
    loadMask: true,
    cm: corpusListCm,
    sm: corpusListSelectionModel,
    flex: 1,
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
        ctCls: 'annis-toolbar-btn',
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

  var formPanelExporter = new Ext.FormPanel({
    id: 'formPanelExport',
    frame:true,
    title: 'Export',
    items: [
      exportSelection,
      padLeftComboBoxExport,
      padRightComboBoxExport,
      additionalParamsExport
    ],
    buttons: [{
      id: 'btnExport',
      text: 'Perform Export',
      disabled: false,
      listeners: {
        click: doExport
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
    width: 340,
    height: 165,
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
    }]
	           	
  });

		    
  var panelSearchModes = new Ext.TabPanel({
    width: 340,
    height: 180,
    activeTab: 0,
    items: [
      formPanelSimpleSearch,
      formPanelExporter
    ]
  });

			
  var panelSearch = new Ext.Panel({
    region: 'west',
    width: 330,
    layout: "vbox",
    items: [formPanelSearch,corpusGrid, panelSearchModes]
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
    height: Math.max(500,Ext.getBody().getViewSize().height - 35),
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
          window.frames.iframeQueryBuilder.queryBuilderUpdateNodeAnnos(corpusStringListFromSelection(corpusListSelectionModel.getSelections()));
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
  
  //added ctrl + enter for getting search results
  new Ext.KeyMap(Ext.get('queryAnnisQL'), {
    key : Ext.EventObject.ENTER,
    ctrl : true,
    fn : getResult
  });
  
  // highlight tutorial
  Ext.get('tutorial').frame('ff0000', 2);
  
});

// end onReady

