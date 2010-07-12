Ext.onReady(function(){
  Ext.QuickTips.init();
	
  
  var menu = new Ext.menu.Menu({
    id: 'levelSelectionMenu'
  });

  var toolbarPartiture = new Ext.Toolbar();
  toolbarPartiture.render('toolbar');
  
  Ext.each(levelNames, function(levelName) {
    menu.add({
      text: levelName,
      id: levelName,
      checked: true,
      checkHandler: onItemCheck
    });
  });

  toolbarPartiture.add({
    text:'Select Displayed Annotation Levels',
    iconCls: 'bmenu',  // <-- icon
    menu: menu  // assign menu by instance
  });


  toolbarPartiture.doLayout();

  function onItemCheck(item, checked){
    var element = Ext.get("level_" + item.getId());
    element.setVisible(checked);
    element.setDisplayed(checked ? "" : "none");
  }

});

function toggleAnnotation(element, isOver) {
  //  var extClassOver = "x-grid3-row-over";
  var el = Ext.get(element);
  
  var tmpAtt = el.getAttributeNS("annis", "tokenIds");
  if(tmpAtt != null)
  {
    var tokenIds = tmpAtt.split(",");
    Ext.each(tokenIds, function(tokenId) 
    {
      var elToken = Ext.get("token_" + tokenId);
      if(elToken != null)
      {
        if(isOver) {
          elToken.addClass('highlightedToken');
        } else {
          elToken.removeClass('highlightedToken');
        }
      }
    });
  }

  tmpAtt = el.getAttributeNS("annis", "eventIds");
  if(tmpAtt != null)
  {
    var eventIds = tmpAtt.split(",");
    Ext.each(eventIds, function(eventId) 
    {
      var elToken = Ext.get("event_" + eventId);
      if(elToken != null)
      {
        if(isOver) {
          elToken.addClass('highlightedEvent');
        } else {
          elToken.removeClass('highlightedEvent');
        }
      }
    });
  }
}