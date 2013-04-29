window.annis_visualizers_component_pdf_PDFPanel = function() {

  function openPDF(url, id, firstPage, lastPage)
  {
    PDFJS.disableWorker = true;
    PDFJS.getDocument(url).then(function(pdf) {


      // get complete pdf document
      if (firstPage === -1) {
        var pages = pdf.numPages;
        for (var i = 0; i < pages; i++)
        {
          var canvas = initCanvas(id, i + 1);
          renderPage(pdf, i + 1, canvas);
        }
      }
      else {
        for (var i = firstPage - 1; i <= lastPage - 1; i++)
        {
          var canvas = initCanvas(id, i + 1);
          renderPage(pdf, i + 1, canvas);
        }
      }
    });
  }

  function initCanvas(id, pageNumber) {
    var wrapperElem = document.getElementById(id);
    canvas = document.createElement('canvas');
    wrapperElem.appendChild(canvas);
    canvas.setAttribute("id", "canvas-" + id + "-page-" + pageNumber);
    canvas.style.position = "relative";

    return canvas;
  }

  function renderPage(pdf, pageNumber, canvas) {
    pdf.getPage(pageNumber).then(function(page) {
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
    var firstPage = this.getState().firstPage;
    var lastPage = this.getState().lastPage;

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
      openPDF(url, id, firstPage, lastPage);
    }
  };
};