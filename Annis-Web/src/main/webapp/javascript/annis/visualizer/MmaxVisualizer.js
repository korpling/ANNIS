Ext.onReady(function(){
  Ext.QuickTips.init();
	

});

function markText(id, mark) { 
  var extClassOver = "x-grid3-row-over";
  try {
    for(var groupId in discourseGroups) {
      var tokenIds = discourseGroups[groupId];
      if(tokenIds.indexOf(id) != -1) {		
        if(document.getElementById('checkbox_group_' + groupId).checked) {
          for(var i=0; i<tokenIds.length; i++) {
            var el = Ext.get(tokenIds[i]);
            if(mark) {
              el.applyStyles('background-color:#faf755;');
            } else {
              el.applyStyles('background-color:#a4e4ff;');
            }
          }
        }
      }
    }
  } catch(e) {
    //ignore
  }
}

function toggleGroupClass(groupId) {
  //try {
  for(var key in discourseGroups[groupId]) {
    var element = document.getElementById(discourseGroups[groupId][key]);
    var el = Ext.get(element);
    try {
      if(el.hasClass(groupId)) {
        el.removeClass(groupId);
        el.applyStyles('background-color:#ffffff;');
      } else {
        el.addClass(groupId);
        el.applyStyles('background-color:#a4e4ff;');
      }
    } catch (e) {
      console.error(e);
    }
			
  }
  //} catch (e) {
  //	console.error(e);
  //}
  markText(discourseGroups[groupId][0], true);
  setTimeout('markText("' +  discourseGroups[groupId][0] + '", false);',500);
}