var activationMap = new Array();
var linkMap = new Array();
var activationOrderMap = new Array();
var colorMap = new Array();

var currentNumberIndex = 1;//0;

Ext.onReady(function()
{
  Ext.QuickTips.init();
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
  togglePRAutoWithClass(el, "highlightedToken");
}

function togglePRAutoWithClass(el, className)
{
  var element = Ext.get(el);
  //var isOn = element.hasClass(className);
  var idAtt = element.getAttributeNS("annis","pr_right");
  var isOn = linkMap[idAtt]==1;
  if (linkMap[idAtt]==1) {
      linkMap[idAtt]=-1;
  } else {
      linkMap[idAtt]=1;
  }
  currentNumberIndex++;
  togglePRWithClass(element, !isOn, className);
}

function togglePRWithClass(el, on, className)
{
  var element = Ext.get(el);
  var idAtt = element.getAttributeNS("","id");

  if(element != null && idAtt != null)
  {
    var attLeft = element.getAttributeNS("annis", "pr_left");
    if(attLeft != null)
    {
      var prIDsL = attLeft.split(',');

      Ext.each(prIDsL, function(pr)
      {
        var elToken = Ext.get("tok_" + pr);
        if(elToken != null)
        {
          togglePRWithClassFinal(elToken, on, className, el);
        }
      });
    }

  }

}

function togglePRWithClassFinal(el, on, className, superEl)
{
  var element = Ext.get(el);
  var idAtt = element.getAttributeNS("","id");
  var sElement = Ext.get(superEl);
  var superIdAtt = sElement.getAttributeNS("annis","pr_right");

  if(element != null && idAtt != null)
  {
    var id = 1*(idAtt.substring("tok_".length));

    if(on)
    {
        element.addClass(className);
        if (activationOrderMap[idAtt] == null){
            activationOrderMap[idAtt] = "";
        }
        activationOrderMap[idAtt] += superIdAtt+",";
        colorMap[superIdAtt] = currentNumberIndex;
        element.setStyle("background-color", getRGBFromNumber(currentNumberIndex));
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
        element.setStyle("background-color", "rgb(255,255,255)");
        activationMap[id] = -1;
      } else {
        element.setStyle("background-color", getRGBFromNumber(colorMap[recolor]));//colorMap[superIdAtt]));//"rgb(150,255,150)");
      }
    }
  }
}