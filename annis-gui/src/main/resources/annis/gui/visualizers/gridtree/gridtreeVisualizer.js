function highlightedToken(element, lastRow, changeClass)
{

  element.addClass("hover");
  
  // fetch the intervall for highlight the tokens. the form is:
  // id="interval:hashcode:n-m:namespace:anno=anno_value"
  var intervall = element.attr('id').split(':');
  var left = intervall[2].split('-')[0];
  var right = intervall[2].split('-')[1];

  $.each(lastRow, function(unusedIndex, td)
  {
    var index = td.cellIndex;
    if (index >= left && index <= right)
      changeClass($(td));
  });
}

$(document).ready(function()
{
  var lastRow = $("#gridtree-partitur tr:last td");

    
  $("#gridtree-partitur *[colspan]").bind({
    mouseover : function(e)
    {
      highlightedToken($(this), lastRow, function(element)
      {        
        element.addClass("highlightedToken");
      });
    },
    mouseout : function(e)
    {
      highlightedToken($(this), lastRow, function(element)
      {
        element.removeClass("highlightedToken");
      });
    }
  });
  
  $("#gridtree-partitur *[colspan]").tooltip();

});