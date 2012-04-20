var startTime;
var endTime = null;

/**
 * this function is the interface for the KWIC-Visualizer, which search for 
 * this global function in the DOM, so changing the signature will probably 
 * causes serious problems
 */
var seekAndPlay = function (start, end) 
{  
  ($("video")[0] || $("audio")[0]).currentTime = start;  
  startTime = start;
  endTime = end;
};


/**
 * this is function is invoked by the KWICPanel, when a media visualizer
 * is collapsing
 */
var stop = function ()
{
  ($("video")[0] || $("audio")[0]).pause();
}

$(document).ready(function()
{	  
  var media = $($("video")[0] || $("audio")[0]);
  media[0].pause();	
  

  media.on("seeked", function()
  {
    media[0].play();
  }); 
  
  media.on("timeupdate", function()
  {
    if (endTime !== null && media[0].currentTime >= endTime)
    {       
      media[0].pause();      
    }    
  });

  media.on("pause", function()
  {
    // after stopping the media, we don't want to stop the media again, after 
    // press play again
    endTime = null;
  });

  media.on("play", function()
  {
    lookAndSwitchCurrentMediaPlayer(media[0]);
  });
});

function lookAndSwitchCurrentMediaPlayer(mediaElement)
{  

  if (!window.parent.document.mediaElement)
  {
    window.parent.document.mediaElement = mediaElement;
    return;
  }

  if(window.parent.document.mediaElement !== mediaElement) {
    window.parent.document.mediaElement.pause();
    window.parent.document.mediaElement = mediaElement;
  }  
}


