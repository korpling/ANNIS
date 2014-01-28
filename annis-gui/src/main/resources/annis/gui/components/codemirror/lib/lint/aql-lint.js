CodeMirror.registerHelper("lint", "aql", function(text) {
  var found = [];
  
  // test error
  found.push({
    from: CodeMirror.Pos(1, 1),
    to: CodeMirror.Pos(1, 3),
    message: "Just a test error"
  });
  
  return found;
});
