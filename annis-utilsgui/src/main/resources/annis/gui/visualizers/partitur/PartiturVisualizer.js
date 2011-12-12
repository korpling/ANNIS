$(document).ready(function(){
  
  $(".single_event").tooltip();
  
  $("#toolbar").append('<li><a href="#">Select Displayed Annotation Levels</a>'
    + '<ul id="levelselector"></ul>');

  $.each(levelNames, function(index, levelName) { 
        
    $("#levelselector").append(
      '<li><a href="#" class="checkedItem" id="a_' + levelName +'" >'
      + levelName + '</a></li>');
    
    var linkElem = $("#a_" + levelName);
    
    linkElem.click(function(ev){
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
      
      // ignore the "#" href-information (and therefore do not jump on page)
      ev.preventDefault();
    });
  });

  $("#toolbar").jbar();
  

});

function toggleAnnotation(element, isOver) {

  var el = $(element);
  
  var tmpAtt = el.attr("annis:tokenIds");
  if(tmpAtt != null)
  {
    var tokenIds = tmpAtt.split(",");
    $.each(tokenIds, function(index, tokenId) 
    {
      var elToken = $("#token_" + tokenId);
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

  tmpAtt = el.attr("annis:eventIds");
  if(tmpAtt != null)
  {
    var eventIds = tmpAtt.split(",");
    $.each(eventIds, function(index, eventId) 
    {
      var elToken = $("#event_" + eventId);
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
