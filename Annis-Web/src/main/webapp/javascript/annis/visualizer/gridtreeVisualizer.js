Ext.onReady(function()
{
  var tableCells = Ext.select("#gridtree-partitur *[colspan]", true);
  var lastRow = Ext.select("#gridtree-partitur tr:last td");

  console.log(tableCells);

  tableCells.each(function(el)
  {
    el.on({
      'mouseover' : {
        fn : function(e, t)
        {

          var element = Ext.get(t);
          element.addClass("hover");
          var colspan = element.getAttribute('colspan');

          // fetch the intervall for highlight the tokens. the form is:
          // id="interval:n-m"
          var intervall = element.getAttribute('id').split(':');
          var left = intervall[1].split('-')[0];
          var right = intervall[1].split('-')[1];

          lastRow.each(function(td)
          {
            var index = td.getAttribute('cellIndex');
            if (index >= left && index <= right)
              td.addClass('highlightedToken');
          });

        }
      },
      'mouseout' : {
        fn : function(e, t)
        {
          var element = Ext.get(t);
          element.removeClass("hover");
          var colspan = element.getAttribute('colspan');

          // fetch the intervall for highlight the tokens. the form is:
          // id="interval:n-m"
          var intervall = element.getAttribute('id').split(':');
          var left = intervall[1].split('-')[0];
          var right = intervall[1].split('-')[1];

          lastRow.each(function(td)
          {
            var index = td.getAttribute('cellIndex');
            if (index >= left && index <= right)
              td.removeClass('highlightedToken');
          });
        }
      }
    });
  })

});