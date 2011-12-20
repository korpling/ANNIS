var testTime = 200;

$(document).ready(function() {  
  $("audio:first").player.currentTime = testTime;  
  $("audio:first").on("seeked", function(event){
    console.log(event.type);
  });  
});