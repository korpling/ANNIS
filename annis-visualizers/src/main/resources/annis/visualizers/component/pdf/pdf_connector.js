window.annis_visualizers_component_pdf_PDFPanel = function() {

  function openPDF(url, id, pageNumber)
  {
    PDFJS.disableWorker = true;
    PDFJS.getDocument(url).then(function(pdf) {

      if (pageNumber === -1) {
        var pages = pdf.numPages;
        for (i = 0; i < pages; i++)
        {
          var canvas = initCanvas(id, i + 1);
          renderPage(pdf, i + 1, canvas);
        }

      } else {
        var canvas = initCanvas(id, pageNumber);
        renderPage(pdf, pageNumber, canvas);
      }
    });
  }

  function initCanvas(id, pageNumber) {
    var wrapperElem = document.getElementById(id);
    canvas = document.createElement('canvas');
    wrapperElem.appendChild(canvas);
    canvas.style.width = wrapperElem.style.width + "px";
    canvas.style.height = wrapperElem.style.height + "px";
    canvas.setAttribute("width", wrapperElem.style.width);
    canvas.setAttribute("height", wrapperElem.style.height);
    canvas.setAttribute("id", "canvas-" + id + "-page-" + pageNumber);
    canvas.style.position = "relative";

    return canvas;
  }
  ;

  function renderPage(pdf, pageNumber, canvas) {
    pdf.getPage(pageNumber).then(function getPageHelloWorld(page) {
      var scale = 1.5;
      var viewport = page.getViewport(scale);

      canvas.height = viewport.height;
      canvas.width = viewport.width;

      page.render({
        canvasContext: canvas.getContext("2d"),
        viewport: viewport});
    });
  }


  this.onStateChange = function()
  {
    var url = this.getState().binaryURL;
    var id = this.getState().pdfID;
    var page = this.getState().page;

    // cleanup old canvas elements
    if (document.getElementById("canvas-" + id) !== undefined)
    {
      var wrapper = document.getElementById(id);
      while (wrapper.hasChildNodes())
      {
        wrapper.removeChild(wrapper.lastChild);
      }
    }

    if (url !== undefined)
    {
      openPDF(url, id, page);
    }

    console.log(url + ", " + id, page);
  };
};