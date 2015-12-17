window.annis_visualizers_component_visjs_VisJsComponent = 
  function() {
    // Create the component
    var mycomponent =
        new vis.Network(null, null, null);
    
    // Handle changes from the server-side
    this.onStateChange = function() {
        mycomponent.setValue(this.getState().value);
    };

    // Pass user interaction to the server-side
    var self = this;
    mycomponent.click = function() {
        self.onClick(mycomponent.getValue());
    };
};
