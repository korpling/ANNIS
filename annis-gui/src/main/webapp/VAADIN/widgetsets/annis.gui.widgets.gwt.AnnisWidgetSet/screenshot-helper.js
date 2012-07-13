/* 
 * html2canvas v0.30 <http://html2canvas.hertzen.com>
 * Copyright (c) 2011 Niklas von Hertzen. All rights reserved.
 * http://www.twitter.com/niklasvh 
 * 
 * Released under MIT License
 * Modified for the use in js-screenshot vaadin addon.
 */

/*
 * The MIT License

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

var screenshot = function(element, options) {
	var date = new Date();
	var message, timeoutTimer, timer = date.getTime();
        
	var preload = html2canvas.Preload(element, {
		"complete" : function(images) {
			var queue = html2canvas.Parse(element, images);
			var canvas = $(html2canvas.Renderer(queue));
			var finishTime = new Date();

			var data = canvas.context.toDataURL();
			options.received(data);

			if (options.showMessage) {
				throwMessage('Screenshot created in '
					+ ((finishTime.getTime() - timer) / 1000)
					+ " seconds<br />", 4000);
		    }
		}
	});

	function throwMessage(msg, duration) {

		window.clearTimeout(timeoutTimer);
		timeoutTimer = window.setTimeout(function() {
			message.fadeOut(function() {
				message.remove();
			});
		}, duration || 2000);
		$(message).remove();
		message = $('<div />').html(msg).css({
			margin : 0,
			padding : 10,
			background : "#000",
			opacity : 0.7,
			position : "fixed",
			top : 10,
			right : 10,
			fontFamily : 'Tahoma',
			color : '#fff',
			fontSize : 12,
			borderRadius : 12,
			width : 'auto',
			height : 'auto',
			textAlign : 'center',
			textDecoration : 'none'
		}).hide().fadeIn().appendTo('body');
	}
};
