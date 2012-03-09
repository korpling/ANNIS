var startTime;
var endTime = null;

/**
 * this function is the interface for the KWIC-Visualizer, which search for 
 * this global function in the DOM, so changing the signature will probably 
 * causes serious problems
 */
var seekAndPlay = function (start, end) 
{  
  var mediaElement = $("video")[0] || $("audio")[0];
  
  mediaElement.currentTime = start;  
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
  var mediaElement; 
  
  if ($("video"))
    mediaElement = $("video"); 
  
  if ($("audio"))
    mediaElement = $("audio");
    
  mediaElement[0].pause();	  

  mediaElement.on("seeked", function()
  {
    mediaElement[0].play();
  }); 
  
  mediaElement.on("timeupdate", function()
  {
    if (endTime !== null && mediaElement[0].currentTime >= endTime)
    {
      mediaElement[0].pause();
      endTime = null;
    }
  });

  mediaElement.on("pause", function()
  {
    // after stopping the mediaElement, we don't want to stop the mediaElement again, after press play again
    endTime = null;
  });

  mediaElement.on("play", function()
  {
    lookAndSwitchCurrentMediaPlayer(mediaElement[0]);
  });
});

function lookAndSwitchCurrentMediaPlayer(videoElement)
{  

  if (!window.parent.document.mediaElement)
  {
    window.parent.document.mediaElement = videoElement;
    return;
  }

  if(window.parent.document.mediaElement !== videoElement) {
    window.parent.document.mediaElement.pause();
    window.parent.document.mediaElement = videoElement;
  }  
}


