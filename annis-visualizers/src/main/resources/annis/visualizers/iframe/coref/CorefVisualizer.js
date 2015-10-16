var activationMap = new Array();
var linkMap = new Array();
var activationOrderMap = new Array();
var colorMap = new Array();

var currentNumberIndex = 1;//0;

$(document).ready(function()
{
  $("td[title]").tooltip({
    // no animation
    show: false,
    content: function () {
        return $(this).attr('title');
    }
  });
});

function getRGBFromNumber(i)
{
  var r = (((i)*224) % 255);
  var g = (((i + 197)*1034345) % 255);
  var b = (((i + 23)*74353) % 255);


  //  too dark or too bright?
  if(((r+b+g) / 3) < 100 )
  {
    r = 255 - r;
    g = 255 - g;
    b = 255 - b;
  }
  else if(((r+b+g) / 3) > 192 )
  {
    r = 1*(r / 2);
    g = 1*(g / 2);
    b = 1*(b / 2);
  }

  if(r == 255 && g == 255 && b == 255)
  {
    r = 255;
    g = 255;
    b = 0;
  }

  return "rgb(" + r + "," + g + "," + b + ")";
}

function togglePRAuto(el)
{
  
  togglePRAutoWithClass($(el), "highlightedToken");
}

function togglePRAutoWithClass(element, className)
{
  var idAtt = element.attr("annis:pr_right");
  var isOn = linkMap[idAtt]==1;
  if (linkMap[idAtt]==1) {
      linkMap[idAtt]=-1;
  } else {
      linkMap[idAtt]=1;
  }
  currentNumberIndex++;
  togglePRWithClass(element, !isOn, className);
}

function togglePRWithClass(element, on, className)
{
  if(element != null)
  {
    var attLeft = element.attr("annis:pr_left");
    if(attLeft != null)
    {
      var prIDsL = attLeft.split(',');

      $.each(prIDsL, function(index, pr)
      {
        var elToken = $("#tok_" + pr);
        togglePRWithClassFinal(elToken, on, className, element);
      });
    }

  }

}

function togglePRWithClassFinal(element, on, className, superEl)
{
  
  var idAtt = element.attr("id");
  
  var sElement = superEl;
  var superIdAtt = sElement.attr("annis:pr_right");


  if(element != null && idAtt != null)
  {
    
    var id = idAtt.substring("tok_".length);

    if(on)
    {
        element.addClass(className);
        if (activationOrderMap[idAtt] == null){
            activationOrderMap[idAtt] = "";
        }
        activationOrderMap[idAtt] += superIdAtt+",";
        colorMap[superIdAtt] = currentNumberIndex;
        element.attr("style", "background-color:" + getRGBFromNumber(currentNumberIndex));
        activationMap[id] = 1;
    }
    else
    {
      element.removeClass(className);
      var recolor = 0;
      if (activationOrderMap[idAtt]!=null) {
          activationOrderMap[idAtt] = activationOrderMap[idAtt].replace(superIdAtt+",","");
          var text = activationOrderMap[idAtt].split(",",10);
          for(var i=0;i<10;i++){
              var word = text[i];
              if (word != null)
              if (word.length>0) {
                    recolor = word;break;
              }
          }
      }
      if (recolor==0){
        element.attr("style" ,"background-color: rgb(255,255,255)");
        activationMap[id] = -1;
      } else {
        element.attr("style", "background-color:" + getRGBFromNumber(colorMap[recolor]));//colorMap[superIdAtt]));//"rgb(150,255,150)");
      }
    }
  }
}