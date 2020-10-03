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
  
  //  bind cursor pointer to table cells, which has the time attribute
  $("td[time]").bind("hover").css("cursor", "pointer");
  function clickHandler(event)
  {
    // check if one media vis is loaded
    var isLoaded = false;
    for(i = 0; i < mediaIDs.length; i++)
    {
      if (window.parent.document.getElementById(mediaIDs[i]).getElementsByTagName("iframe").length > 0)
      {
        isLoaded = true;
        break;
      }
    }

    if (!isLoaded)
    {
      noty({
        "text":"Please open a media visualizer",
        "layout":"center",
        "type":"error",
        "textAlign":"center",
        "easing":"swing",
        "animateOpen":{
          "height":"toggle"
        },
        "animateClose":{
          "height":"toggle"
        },
        "speed":"500",
        "timeout":"5000",
        "closable":true,
        "closeOnSelfClick":true
      });
    }

    for(i = 0; i < mediaIDs.length; i++)
    {
      var  time = $(this).attr("time");
      var iframe = window.parent.document.getElementById(mediaIDs[i]).
      getElementsByTagName("iframe")[0];
      if (iframe) // check if loaded
      { 
        var s = time.split("-");
        iframe.contentWindow.seekAndPlay(s[0]*1, s[1]*1);
      }
    }    
  }

  /**
   *  iterate over all media vis and call seekAndPlay()-function. The ids of the media vis are saved in a
   *  global array mediaIDs.
   *
   */
  $("td[time]").each(function ()
  {    
    $(this).click(clickHandler);
    $(this).addClass("speaker");
  });

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
