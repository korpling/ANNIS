$(document).ready(function(){
  
  
  /*
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
*/

  $("#toolbar").append('<li><a href="#">Select Displayed Annotation Levels</a>'
    + '<ul id="levelselector"></ul>');

  $.each(levelNames, function(index, levelName) { 
        
    $("#levelselector").append(
      '<li><a href="#" class="checkedItem" id="a_' + levelName +'" >'
      + levelName + '</a></li>');
    
    var linkElem = $("#a_" + levelName);
    
    linkElem.click(function(){
      var checked = !linkElem.hasClass("checkedItem");
            
      linkElem.removeClass("checkedItem");
      linkElem.removeClass("uncheckedItem");
      
      linkElem.addClass(checked ? "checkedItem" : "uncheckedItem");      

      if(checked)
      {
        $(".level_" + levelName).show();
      }
      else
      {
        $(".level_" + levelName).hide();
      }
    });
  });

  $("#toolbar").jbar();
/*
  function onItemCheck(item, checked){
    var elements = Ext.query(".level_" + item.getId());
    Ext.each(elements, function(domitem,index)     {
      var item = Ext.get(domitem);
      item.setVisible(checked);
      item.setDisplayed(checked ? "" : "none")
    });
  }
*/
//var element = Ext.get("level_" + item.getId());

});

function toggleAnnotation(element, isOver) {
/*
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
  */
}
