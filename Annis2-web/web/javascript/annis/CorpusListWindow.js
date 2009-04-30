Ext.onReady(function()
  {
    Ext.Ajax.defaultHeaders =
    { 
      "Content-Type": "application/x-www-form-urlencoded; charset=utf-8"
    };

    CorpusListWindow = function(id)
    {
      this.storeCorpusList = new Ext.data.JsonStore({
        url: conf_context + '/secure/CorpusList/NoFavorites',
        totalProperty: 'size',
        root: 'list',
        id: 'id',
        fields: [
        {
          name:'id',
          type:'int'
        },
        'name',
        {
          name:'textCount',
          type:'int'
        },

        {
          name:'tokenCount',
          type:'int'
        }
        ],
        // turn on remote sorting
        remoteSort: false
      });
      this.storeCorpusList.setDefaultSort('name', 'asc');

      this.corpusListSelectionModel = new Ext.grid.CheckboxSelectionModel(
      {
        singleSelect : false
      });

      this.corpusListCm = new Ext.grid.ColumnModel([
        this.corpusListSelectionModel,
        {
          header: "Name",
          dataIndex: 'name',
          width: 170,
          align: 'left'
        },{
          header: "Texts",
          dataIndex: 'textCount',
          // width: 60,
          align: 'right'
        },{
          header: "Token",
          dataIndex: 'tokenCount',
          //width: 65,
          align: 'right'
        }
        ]);
      this.corpusListCm.defaultSortable = true;

      this.corpusGrid = new Ext.grid.GridPanel(
      {
        store: this.storeCorpusList,
        viewConfig: {
          forceFit:true,
          autoFill: true
        },
        loadMask: true,
        cm: this.corpusListCm,
        sm: this.corpusListSelectionModel,
        //width: 330,
        //height: 195,
        enableDragDrop:true,
        ddGroup: 'corpusList',
        stripeRows: true,
        title:'Available Corpora',
        header: false
      });

      config =
      {
        applyTo: 'windowCorpusList',
        hidden: true,
        title: 'All Available Corpora',
        id: 'windowCorpusList',
        closable:true,
        maximizable: false,
        width:800,
        height:605,
        //border:false,
        plain:true,
        layout: 'fit',
        closeAction: 'hide',
        items: [
         this.corpusGrid
        ]
      }

      // superclass constructor
      CorpusListWindow.superclass.constructor.call(this, config);

      // init stuff
      this.render(id);
      this.storeCorpusList.load();

      //extend dnd extender to allow dnd from corpus window
      
      var finalThis = this;

      var myDrop = new Ext.dd.DropTarget(this.corpusGrid.container, {
        dropAllowed: 'x-dd-drop-ok',
        ddGroup: 'corpusList',
        notifyDrop: function(dd, e, data) {
          var ds=data.grid.getStore();
          var dt = finalThis.corpusGrid.getStore();

          dt.add(data.selections);
          var dtSortState = dt.getSortState();
          dt.sort(dtSortState.field,dtSortState.direction);
          var addedIds = [];
          for(var i=0;i<data.selections.length;i++) {
            ds.remove(data.selections[i]);
            addedIds.push(data.selections[i].id);
          }
          Ext.Ajax.request({
            url: conf_context + '/secure/CorpusList/Favorites',
            method: 'post',
            //success: someFn,
            //failure: otherFn,
            params: {
              remove: addedIds.join(",")
            }
          });
          return true;
        }
      });
     
      
    };
    Ext.extend(CorpusListWindow, Ext.Window);

    var windowCorpusList = new CorpusListWindow('windowCorpusList');

  });