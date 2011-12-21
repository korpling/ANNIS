var startTime;
var endTime = undefined;

/**
 * this function is the interface for the KWIC-Visualizer, which search for 
 * this global function in the DOM, so changing the signature will probably 
 * causes serious problems
 */
var seekAndPlay = function (start, end) 
{  
  $("audio")[0].currentTime = start;
  $("audio")[0].play();
  startTime = start;
  endTime = end;
  
};

$(document).ready(function() {	  
  var audio = $('audio');
  audio[0].pause();   
	
  audio.on("canplaythrough", function() {
    audio[0].currentTime = startTime;
  });
	

  audio.on("seeked", function(){    
    audio[0].play();
  }); 
  
  audio.on("timeupdate", function(){
    if (endTime !== null && audio[0].currentTime >= endTime) 
    {
      audio[0].pause();
    }
  });
});

