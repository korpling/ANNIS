

//todo the loading frame might want to be masked
function waitForIFrame(id) {
  var elem = document.getElementById(id);
  var doc = elem.contentWindow.document;

  if(!doc.body)
  {
    setTimeout('waitForIFrame("' + id + '")', 500);
  }
  else if(doc.contentType == "image/png")
  {
    if(doc.getElementsByTagName('img').length === 0)
    {
      setTimeout('waitForIFrame("' + id + '")', 500);
    }
    else
    {
      var img = doc.getElementsByTagName('img')[0];
      elem.height = img.naturalHeight;
      elem.width = img.naturalWidth;
    }
  }
  else
  {
    if(doc.body.scrollHeight <= 20)
    {
      setTimeout('waitForIFrame("' + id + '")', 500);
    }
    else
    {
      //adjust iframe size
      try
      {
        elem.height = doc.body.scrollHeight;
        elem.width = doc.body.scrollWidth;
      }
      catch (e)
      {
        //alert('waitForIFrame: ' + e);
      }
    }
  }
}

Ext.onReady(function()
{
  var MAX_FIELDS = 50;
  var PAGE_SIZE = 25;

  function getFields() {
    var fields = ['_id', '_textId', '_text', '_levels', '_namespaces',
    '_tokenLevels', '_markedObjects', '_matchStart', '_matchEnd'];
    for(var i=0; i<MAX_FIELDS; i++) {
      fields.push('' + i);
    }
    return fields;
  }

  // create the Data Store
  var tokenLevels = [];
  var storeSearchResult = new Ext.data.JsonStore({
    proxy: new Ext.data.HttpProxy({
      url: conf_context + '/secure/SearchResult',
      timeout: global_timeout
    }),

    id: 'storeSearchResult',
    root: 'resultSet',
    totalProperty: 'totalCount',
    fields: getFields(),

    // turn on remote sorting
    remoteSort: true,
    listeners : {

      'load' : function()
      {
        var isRTL = false;

        this.each(function(record)
        {
          // check if left to right or right to left text order
          for(var i=0;!isRTL && i < record.data._text.length; i++)
          {
            var cc = record.data._text.charCodeAt(i);
            // hebrew extended and basic, arabic basic and extendend
            if(cc >= 1425 && cc <=1785)
            {
              isRTL = true;
            }
            // alphabetic presentations forms (hebrwew) to arabic presentation forms A
            else if(cc >= 64286 && cc <= 65019)
            {
              isRTL = true;
            }
            // arabic presentation forms B
            else if(cc >= 65136 && cc <= 65276)
            {
              isRTL = true;
            }
          }

          //lets update the token level selection button
          Ext.each(record.data._tokenLevels, function(item) {
            if(tokenLevels.indexOf(item) == -1) {
              tokenLevelSelectionMenu.addItem(new Ext.menu.CheckItem({
                id: 		item.replace(':', '_'),
                text: 		item,
                checked: 	true,
                checkHandler: onTokenLevelCheck
              }));
              tokenLevels.push(item);
            }
          });
        });

        if(isRTL)
        {
          // patch style attribute
          var elements = Ext.query('*.windowSearchResult.token.match');
          Ext.each(elements, function(e)
          {
            e.style.cssFloat = "right";
          });
          elements = Ext.query('*.windowSearchResult.token.postmatch');
          Ext.each(elements, function(e)
          {
            e.style.cssFloat = "right";
          });
          elements = Ext.query('*.windowSearchResult.token.prematch');
          Ext.each(elements, function(e)
          {
            e.style.cssFloat = "left";
          });
        }
      }
    }
  });

  function onTokenLevelCheck(item, checked) {
    var elements = Ext.query('div[class~=' + item.getItemId() + ']');
    Ext.each(elements, function(element) {
      element.style.display = (checked) ? 'block' : 'none';
    });
  }

  var tokenLevelSelectionMenu = new Ext.menu.Menu({
    id: 'mainMenu',
    items: [ ]
  });

  function autosizeColumns(){
    var columnModel = gridSearchResult.getColumnModel();
    var gridView = gridSearchResult.getView();
    //process all columns and adjust their size

    // fixed size for actions
    columnModel.setColumnWidth(0, 24);

    // rest
    var widthTotal = 24;
    for(i = 1; i<4; i++) { //columns
      var width = 0;
      for(j = 0; j<PAGE_SIZE; j++) //rows
      {
        try
        {

          var cell = gridView.getCell(j, i);
          var divs = Ext.DomQuery.select("div.token", cell);
          var divWidth = 0;
          if(divs !== null)
          {
            Ext.each(divs, function(div)
            {
              var offset = div.offsetWidth;
              if(offset > -1)
              {
                divWidth += offset + 5;
              }
            });
          }
          if(divWidth > width)
          {
            width = divWidth;
          }
        } catch (e) {
        // tried to access invalid cell, ignore
        }
      }
      width += 30;
      if(i==3 && widthTotal + width < windowSearchResult.getInnerWidth() - 30) //extend last column to fit window
      {
        width = windowSearchResult.getInnerWidth() - 30 - widthTotal;
      }
      widthTotal += width;
      columnModel.setColumnWidth(i, width);
    }
  }

  var pagingToolbar = new Ext.PagingToolbar({
    pageSize: PAGE_SIZE,
    store: storeSearchResult,
    displayInfo: true,
    displayMsg: 'Displaying Results {0} - {1} of {2}',
    emptyMsg: "No Results to display.",
    items: [
    {
      text:'Token Annotations',
      iconCls: 'windowSearchResult menu tokenAnnotations',
      menu: tokenLevelSelectionMenu
    },
    {
      id: 'btnShowCitation',
      text: 'Show Citation URL',
      disabled: false,
      listeners: {
        click: function() {
          Ext.Msg.alert('Citation', '<textarea readonly="f" wrap="virtual" rows="5" cols="60">' + Citation.generate() + "</textarea>");
        }
      }
    }
    ]
  });


  var rowBodyTemplate = new Ext.XTemplate(
    '<div style="width: 100%; margin-left:40px; overflow:auto; text-align: left;">',
    '		<ul class="annis-row-body">' +
    '			<tpl for="_namespaces">' +
    '				<li>' +
    '					<div>' +
    '						<div id="annotation-{[this.cleanId(parent._id)]}-{.}-selector" class="annis-level-selector-collapsed" onclick="toggleRowBody(\'{[this.cleanId(parent._id)]}-{.}\');">' +
    '                           {[values == "" ? "undefined" : values]}' +
    '						</div>' +
    '						<div id="annotation-{[this.cleanId(parent._id)]}-{.}-body" class="annis-level-body">' +
    '								<iframe width="95%" height="20px" frameborder="0" src="' + conf_context + '/empty.html" annis:src=' + conf_context + '/secure/Visualizer?spanId={parent._id}&textId={parent._textId}&namespace={.}&mark:red=<tpl for="parent._markedObjects">{.},</tpl>"></iframe>' +
    '						</div>' +
    '					</div>' +
    '				</li>' +
    '			</tpl>' +
    '				<li>' +
    '					<div>' +
    '						<div id="annotation-{[this.cleanId(values._id)]}-paula-selector" class="annis-level-selector-collapsed" onclick="toggleRowBody(\'{[this.cleanId(values._id)]}-paula\');">' +
    '							Paula' +
    '						</div>' +
    '						<div id="annotation-{[this.cleanId(values._id)]}-paula-body" class="annis-level-body">' +
    '							<iframe width="100%" height="20px" frameborder="0" src="' + conf_context + '/empty.html" annis:src="' + conf_context + '/secure/Visualizer?spanId={_id}&textId={_textId}&namespace=paula&mark:red=<tpl for="_markedObjects">{.},</tpl>"></iframe>' +
    '						</div>' +
    '					</div>' +
    '				</li>' +
    '				<li>' +
    '					<div>' +
    '						<div id="annotation-{[this.cleanId(values._id)]}-paulatext-selector" class="annis-level-selector-collapsed" onclick="toggleRowBody(\'{[this.cleanId(values._id)]}-paulatext\');">' +
    '							Paula Text' +
    '						</div>' +
    '						<div id="annotation-{[this.cleanId(values._id)]}-paulatext-body" class="annis-level-body">' +
    '							<iframe width="100%" height="20px" frameborder="0" src="' + conf_context + '/empty.html" annis:src="' + conf_context + '/secure/Visualizer?spanId={_id}&textId={_textId}&namespace=paulatext&mark:red=<tpl for="_markedObjects">{.},</tpl>"></iframe>' +
    '						</div>' +
    '					</div>' +
    '				</li>' +
    '		</ul>' +
    '</div>',
    {
      cleanId: function(id){
        return id.replace(',', '_');
      }
    });

  function renderToken(value, type, rowData) {
    if(typeof value != 'object')
    {
      return '';
    }
    var marker = '';
    var hit = '';
    try{
      if(value._marker !== '') {
        marker = ' marker' + value._marker.replace('#', '');
        hit = ' hit';
      }
    } catch (e) {
    //ignore
    }
    var output = '';
    output += '<div class="windowSearchResult text">' + value._text + '</div>';
    Ext.each(rowData._tokenLevels, function(tokenLevel) {
      var t = value[tokenLevel] + "";
      if(t == 'undefined')
      {
        t = '&nbsp;';
      }
      output += '<div ext:qtip="' + tokenLevel + ' = ' + t + '" class="windowSearchResult annotation ' + tokenLevel.replace(':', '_') + '">' + t + '</div>';
    });
    return '<div class="windowSearchResult token ' + type + ' ' + hit + marker + '" >' + output + '</div>';
  }

  function renderActions(value, metadata, record, rowIndex, colIndex, store)
  {
    var id = value._corpusId;
    var action = 'new MetaDataWindow(' + id + ').show();';

    var output = '<a href="#" onclick="' + action + '"><img src="' + conf_context + '/images/info.gif"></a>';

    return output;
  }

  function renderPrematch(value, metadata, record, rowIndex, colIndex, store){
    var output = "";
    var row = store.getAt(rowIndex);
    var rowData = row.data;
    var matchStart = rowData._matchStart;
    var matchEnd = rowData._matchEnd;
    for(var key in rowData) {
      if(((key * 1) + '') == key ) {
        if(key*1 < matchStart*1) {
          var token = rowData[key];
          output = renderToken(token, 'prematch', rowData) + output;
        }
      }
    }
    return output;
  }

  function renderMatch(value, metadata, record, rowIndex, colIndex, store){
    var output = "";
    var row = store.getAt(rowIndex);
    var rowData = row.data;
    var matchStart = rowData._matchStart;
    var matchEnd = rowData._matchEnd;
    for(var key in rowData) {
      if(((key * 1) + '') == key ) {
        if(key*1 >= matchStart*1 && key*1 <= matchEnd*1) {
          var token = rowData[key];
          output += renderToken(token, 'match', rowData);
        }
      }
    }
    return output;
  }

  function renderPostmatch(value, metadata, record, rowIndex, colIndex, store){
    var output = "";
    var row = store.getAt(rowIndex);
    var rowData = row.data;
    var matchStart = rowData._matchStart;
    var matchEnd = rowData._matchEnd;
    for(var key in rowData) {
      if(((key * 1) + '') == key ) {
        if(key*1 > matchEnd*1) {
          var token = rowData[key];
          output += renderToken(token, 'postmatch', rowData);
        }
      }
    }
    return output;
  }

  var cmItems = 	[];

  cmItems.push({
    id: 'actions',
    dataIndex: '' + 0,
    renderer: renderActions
  });

  cmItems.push({
    id: 'prematch',
    dataIndex: '' + 0,
    renderer: renderPrematch
  });

  cmItems.push({
    id: 'match',
    dataIndex: '' + 0,
    renderer: renderMatch
  });

  cmItems.push({
    id: 'postmatch',
    dataIndex: '' + 0,
    renderer: renderPostmatch
  });
  var cm = new Ext.grid.ColumnModel(cmItems);
  cm.defaultSortable = false;

  var gridSearchResult = new Ext.grid.GridPanel({
    header: false,
    store: storeSearchResult,
    id: 'gridSearchResult',
    cm: cm,
    viewConfig: {
      //	           forceFit:true,
      //	           autofill: true,
      enableRowBody: true,
      showPreview: true,
      getRowClass : function(record, rowIndex, p, store) {

        //extract namespaces from annotation levels and write them to namespaces
        record.data._namespaces = [];
        Ext.each(record.data._levels, function(level)
        {
          var namespace = '';
          if(level.indexOf(':') > -1)
          {
            namespace = level.replace(/[.:].+$/, '');
          }
          if(record.data._namespaces.indexOf(namespace) == -1) {
            record.data._namespaces.push(namespace);
          }
        });

        if(this.showPreview){
          p.body = rowBodyTemplate.applyTemplate(record.data);
          return 'x-grid3-row-expanded';
        }
        return 'x-grid3-row-collapsed';
      },
      listeners : {
        'refresh' : autosizeColumns
      }
    },
    loadMask: true,
    collapsible: true,
    animCollapse: false,
    trackMouseOver:false,
    enableColumnMove: false,
    autoScroll : true,
    autoExpandColumn: 'postmatch',
    tbar: pagingToolbar
  });


  var windowSearchResult = new Ext.Window({
    hidden: true,
    applyTo : 'windowSearchResult',
    title: 'Search Results',
    id: 'windowSearchResult',
    closable:true,
    maximizable: true,
    width:800,
    height:605,
    //border:false,
    plain:true,
    layout: 'fit',
    closeAction: 'hide',
    items: [
    gridSearchResult
    ]
  });
});

function toggleRowBody(id) {
  var elemBody = Ext.get('annotation-'+ id + '-body');
  var elemSelector = Ext.get('annotation-'+ id + '-selector');
  try {
    var elem = elemBody.child('*[src=' + conf_context + '/empty.html]');
    var dom = elemBody.child('*[src=' + conf_context + '/empty.html]', true);
    dom.src = elem.getAttributeNS('annis', 'src');
    waitForIFrame(dom.id);
  } catch (e) 
  {
    //alert('toggleRowBody: ' + e);
  //ignore
  }
  elemBody.setDisplayed(elemBody.isDisplayed() ? false : 'block');
  elemSelector.toggleClass('annis-level-selector-expanded');
}
