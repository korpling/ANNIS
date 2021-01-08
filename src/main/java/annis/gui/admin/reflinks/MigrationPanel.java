package annis.gui.admin.reflinks;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class MigrationPanel extends Panel {

  /**
   * 
   */
  private static final long serialVersionUID = -6893786947746535332L;


  @Override
  public void attach() {
    super.attach();



    TextField serviceUrl = new TextField("ANNIS service URL");
    TextField serviceUsername = new TextField("Username for ANNIS service");
    TextField servicePassword = new TextField("Password for ANNIS service");
    CheckBox skipExisting = new CheckBox("Skip existing UUIDs");


    FormLayout formLayout =
        new FormLayout(serviceUrl, serviceUsername, servicePassword, skipExisting);

    Button btMigrate = new Button("Start migration");

    VerticalLayout layout = new VerticalLayout(formLayout, btMigrate);
    setContent(layout);
  }

}
