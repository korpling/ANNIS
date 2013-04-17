window.annis_visualizers_component_pdf_PDFPanel = function() {

  this.openPDF = function(url, canvas, pageNumber)
  {
    //
    // Disable workers to avoid yet another cross-origin issue (workers need the URL of
    // the script to be loaded, and dynamically loading a cross-origin script does
    // not work)
    //
    PDFJS.disableWorker = true;

    //
    // Asynchronous download PDF as an ArrayBuffer
    //
    PDFJS.getDocument(url).then(function getPdfHelloWorld(pdf) {
      //
      // Fetch the first page
      //
      pdf.getPage(pageNumber).then(function getPageHelloWorld(page) {
        var scale = 1.5;
        var viewport = page.getViewport(scale);

        //
        // Render PDF page into canvas context
        //


        canvas.height = viewport.height;
        canvas.width = viewport.width;


        page.render({
          canvasContext: canvas.getContext("2d"),
          viewport: viewport});
      });
    });
  };

  this.initCanvas = function(id) {
    var wrapperElem = document.getElementById(id);
    canvas = document.createElement('canvas');
    wrapperElem.appendChild(canvas);
    canvas.style.width = wrapperElem.style.width + "px";
    canvas.style.height = wrapperElem.style.height + "px";
    canvas.setAttribute("width", wrapperElem.style.width);
    canvas.setAttribute("height", wrapperElem.style.height);
    canvas.style.position = "relative";

    return canvas;
  };

  /**
   * Converts a string to an int.
   *
   * @param value
   *            if it is already an int it returns simply the value, otherwise
   *            the value is parsed to int.
   */
  this.stringToInt = function(value) {

    if ((typeof value) === "string") {
      return parseInt(value);
    } else {
      return value;
    }
  };

  this.onStateChange = function()
  {
    var url = this.getState().binaryURL;
    var id = this.getState().pdfID;
    var pageNumber = this.stringToInt(id.split("-")[1]);

    // do not initialize twice
    if (document.getElementById(id).getElementsByTagName("canvas")[0])
    {
      return;
    }

    if (url !== undefined)
    {
      this.openPDF(url, this.initCanvas(id), pageNumber + 1);
    }

    console.log(url + ", " + id, pageNumber);
  };
};