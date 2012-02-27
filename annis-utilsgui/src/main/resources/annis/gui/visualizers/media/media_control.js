var startTime;
var endTime = null;

/**
 * this function is the interface for the KWIC-Visualizer, which search for 
 * this global function in the DOM, so changing the signature will probably 
 * causes serious problems
 */
var seekAndPlay = function (start, end) 
{  
  $("video")[0].currentTime = start;  
  startTime = start;
  endTime = end;
};


/**
 * this is function is invoked by the KWICPanel, when a media visualizer
 * is collapsing
 */
var stop = function ()
{
  $("video")[0].pause();
}

$(document).ready(function()
{	  
  var video = $('video');
  video[0].pause();	
  

  video.on("seeked", function()
  {
    video[0].play();
  }); 
  
  video.on("timeupdate", function()
  {
    if (endTime !== null && video[0].currentTime >= endTime)
    {
      video[0].pause();
      endTime = null;
    }
  });

  video.on("pause", function()
  {
    // after stopping the video, we don't want to stop the video again, after press play again
    endTime = null;
  });

  video.on("play", function()
  {
    lookAndSwitchCurrentMediaPlayer(video[0]);
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


