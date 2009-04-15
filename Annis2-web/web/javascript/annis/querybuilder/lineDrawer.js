

var LineDrawer = {
  drawLine : function( canvas, Ax, Ay, Bx, By) {
    ctx = canvas.getContext('2d');
    ctx.beginPath();
						
    ctx.moveTo(Ax,Ay);
    ctx.lineTo(Bx,By);
    ctx.stroke();
    ctx.closePath();
  },
  drawArrow : function(canvas, x, y, direction, arrowLength) {
    
    
    var dir1 = direction + Math.PI/8;
    var dir2 = direction - Math.PI/8;
    
    var end1_x = x - arrowLength * Math.cos(dir1);
    var end1_y = y - arrowLength * Math.sin(dir1);
    
    var end2_x = x - arrowLength * Math.cos(dir2);
    var end2_y = y - arrowLength * Math.sin(dir2);
    
    LineDrawer.drawLine(canvas, x, y, end1_x, end1_y);
    LineDrawer.drawLine(canvas, x, y, end2_x, end2_y);
    
  },
  clear : function(canvas) {
    ctx = canvas.getContext('2d');
    ctx.clearRect(0,0,1000,1000);
  }
};