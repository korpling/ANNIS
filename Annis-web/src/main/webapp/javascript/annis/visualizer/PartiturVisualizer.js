Ext.onReady(function(){
  Ext.QuickTips.init();
	
  var toolbarPartiture = new Ext.Toolbar({
    applyTo: 'toolbar'
  });
	
  var menu = new Ext.menu.Menu({
    id: 'levelSelectionMenu'
  });
	
  Ext.each(levelNames, function(levelName) {
    menu.add({
      text: levelName,
      id: levelName,
      checked: true,
      checkHandler: onItemCheck
    });
  });
	
  toolbarPartiture.add(
  {
    text:'Select Displayed Annotation Levels',
    iconCls: 'bmenu',  // <-- icon
    menu: menu  // assign menu by instance
  }
);

  function onItemCheck(item, checked){
    var element = Ext.get("level_" + item.getId());
    element.setVisibilityMode(Element.DISPLAY);
    if(checked) {
      element.show(true);
    } else {
      element.hide(true);
    }
  }

});

function toggleAnnotation(element, isOver) {
  var extClassOver = "x-grid3-row-over";
  var el = Ext.get(element);
  var cellIndex = el.getAttributeNS("", "cellIndex") * 1;
  var colSpan = el.getAttributeNS("", "colSpan") * 1;
  var tokenIds = el.getAttributeNS("annis", "tokenIds").split(",");
  if(isOver) {
    el.addClass(extClassOver);
  } else {
    el.removeClass(extClassOver);
  }
  Ext.each(tokenIds, function(tokenId) {
    var elToken = Ext.get("token_" + tokenId);
    if(isOver) {
      elToken.applyStyles('background-color:#faf755');
    } else {
      elToken.applyStyles('background-color:#ffffff');
    }
  });
}