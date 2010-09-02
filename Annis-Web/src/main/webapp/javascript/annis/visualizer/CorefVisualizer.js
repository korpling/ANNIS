var activationMap = new Array();
var linkMap = new Array();

var currentNumberIndex = 0;

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
          togglePRWithClassFinal(elToken, on, className);
        }
      });
    }

  }

}

function togglePRWithClassFinal(el, on, className)
{
  var element = Ext.get(el);
  var idAtt = element.getAttributeNS("","id");

  if(element != null && idAtt != null)
  {
    var id = 1*(idAtt.substring("tok_".length));
    var isAlreadyHandled = on ? (activationMap[id] == 1) : (activationMap[id] == -1);

    /*if(isAlreadyHandled)
    {
      return;
      //on=!on;//test
    }//*/

    if(on)
    {
      element.addClass(className);
      element.setStyle("background-color", getRGBFromNumber(currentNumberIndex));
      activationMap[id] = 1;
    }
    else
    {
      element.removeClass(className);
      element.setStyle("background-color", "rgb(255,255,255)");
      activationMap[id] = -1;
    }
  }
}

/**function togglePRWithClass(el, on, className)
{
  var element = Ext.get(el);
  var idAtt = element.getAttributeNS("","id");

  if(element != null && idAtt != null)
  {
    var id = 1*(idAtt.substring("tok_".length));
    var isAlreadyHandled = on ? (activationMap[id] == 1) : (activationMap[id] == -1);

    if(isAlreadyHandled)
    {
      return;
    }

    if(on)
    {
      element.addClass(className);
      element.setStyle("background-color", getRGBFromNumber(currentNumberIndex));
      activationMap[id] = 1;
    }
    else
    {
      element.removeClass(className);
      element.setStyle("background-color", "rgb(255,255,255)");
      activationMap[id] = -1;
    }

    var attLeft = element.getAttributeNS("annis", "pr_left");
    var attRight = element.getAttributeNS("annis", "pr_right");
    if(attLeft != null)
    {
      var prIDsL = attLeft.split(',');

      Ext.each(prIDsL, function(pr)
      {
        var elToken = Ext.get("tok_" + pr);
        if(elToken != null)
        {
          togglePRWithClass(elToken, on, className);
        }
      });
    }

    if(attRight != null)
    {
      var prIDsR = attRight.split(',');

      Ext.each(prIDsR, function(pr)
      {
        var elToken = Ext.get("tok_" + pr);
        if(elToken != null)
        {
          togglePRWithClass(elToken, on, className);
        }
      });
    }

  }//*/