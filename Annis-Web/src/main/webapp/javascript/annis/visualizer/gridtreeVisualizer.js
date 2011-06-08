function highlightedToken(e, t, lastRow, changeClass)
{

  var element = Ext.get(t);
  element.addClass("hover");
  var colspan = element.getAttribute('colspan');

  // fetch the intervall for highlight the tokens. the form is:
  // id="interval:hashcode:n-m"
  var intervall = element.getAttribute('id').split(':');
  var left = intervall[intervall.length - 1].split('-')[0];
  var right = intervall[intervall.length - 1].split('-')[1];

  lastRow.each(function(td)
  {
    var index = td.getAttribute('cellIndex');
    if (index >= left && index <= right)
      changeClass(td);
  });
}

Ext.onReady(function()
{
  var tableCells = Ext.select("#gridtree-partitur *[colspan]", true);
  var lastRow = Ext.select("#gridtree-partitur tr:last td");

  tableCells.each(function(el)
  {
    el.on({
      'mouseover' : {
        fn : function(e, t)
        {
          highlightedToken(e, t, lastRow, function(element)
          {
            element.addClass("highlightedToken");
          });
        }
      },
      'mouseout' : {
        fn : function(e, t)
        {
          highlightedToken(e, t, lastRow, function(element)
          {
            element.removeClass("highlightedToken");
          });
        }
      }
    });
  })

});