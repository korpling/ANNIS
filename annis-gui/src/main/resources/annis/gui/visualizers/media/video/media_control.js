var endTime = undefined;

/**
 * this function is the interface for the KWIC-Visualizer, which search for 
 * this global function in the DOM, so changing the signature will probably 
 * causes serious problems
 */
var seekAndPlay = function (start, end) 
{  
  $("video")[0].currentTime = start;
  $("video")[0].play();
  endTime = end;
};

$(document).ready(function() {	  
  var video = $('video');
  video[0].pause();   
	
  video.on("canplaythrough", function() {
    video[0].currentTime = startTime;
  });
	

  video.on("seeked", function(){    
    video[0].play();
  }); 
  
  video.on("timeupdate", function(){
    if (endTime !== null && currentTime >= endTime) 
    {
      video[0].pause();
    }
  });
});

