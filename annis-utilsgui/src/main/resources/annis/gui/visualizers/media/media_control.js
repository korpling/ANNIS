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
  $("video")[0].play();
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
  enterGlobalMediaVisArray(video[0]);
	
  video.on("canplaythrough", function()
  {
    video[0].currentTime = startTime;
  });	

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
    stopAllOtherPlayer(video[0]);
  });
});

function enterGlobalMediaVisArray(videoElement)
{
  if (! window.parent.document.mediaElement)
  {  
    window.parent.document.mediaElement = [videoElement];
  }
  else {
    window.parent.document.mediaElement.push(videoElement);
  }
  
}

function stopAllOtherPlayer(videoElement)
{
  var globalPlayerList = window.parent.document.mediaElement;
  for (i = 0; i < globalPlayerList.length; i++)
  {
    if (videoElement !== globalPlayerList[i])
    {
      globalPlayerList[i].pause();
    }
  }
}

