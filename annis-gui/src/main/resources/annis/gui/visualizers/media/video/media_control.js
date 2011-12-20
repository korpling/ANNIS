var startTime;
var endTime;


var seekAndPlay = function (start, end) 
{  
  $("video")[0].currentTime = start;
  endTime = end;
  $("video")[0].play();
};

var hideVideo = function ()
{
  $('video').hide();
}

$(document).ready(function() {	
  // put this function to the dom, so we can access it from outside of 
  // the iframe  
  $('html')[0].seekAndPlay = seekAndPlay;   
  $('html')[0].hideVideo = hideVideo;   
  var video = $('video');
  video[0].pause();   
	
  video.on("canplaythrough", function() {
    video[0].currentTime = startTime;
  });
	

  video.on("seeked", function(event){
    console.log(event.type);
    console.log(video[0].currentTime);
    video[0].play();
  });
    
  video.on("timeupdate", function(event){
    console.log(event.type);
    console.log(video[0].currentTime);
    if (video[0].currentTime > 20)
      video[0].pause();    
  });
  
});

