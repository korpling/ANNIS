package annis.visualizers.component.visjs;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.jsoup.nodes.Element;

import com.vaadin.server.ClientMethodInvocation;
import com.vaadin.server.ErrorHandler;
import com.vaadin.server.Extension;
import com.vaadin.server.Resource;
import com.vaadin.server.ServerRpcManager;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.shared.communication.SharedState;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.declarative.DesignContext;

import elemental.json.JsonObject;

public class VisJsController implements Component{
	
	public VisJsController(){
		final VisJsComponent mycomponent = new VisJsComponent("Hello!");
		HorizontalLayout grid = new HorizontalLayout();

		// Set the value from server-side
		mycomponent.setValue("Server-side value");

		// Process a value input by the user from the client-side
		mycomponent.addValueChangeListener(
		        new VisJsComponent.ValueChangeListener() {
		    @Override
		    public void valueChange() {
		        Notification.show("Value: " + mycomponent.getValue());
		    }
		});


		grid.addComponent(mycomponent);
	

		//setContent(grid);

	}

	@Override
	public void addAttachListener(AttachListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeAttachListener(AttachListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDetachListener(DetachListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeDetachListener(DetachListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ClientMethodInvocation> retrievePendingRpcCalls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConnectorEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<? extends SharedState> getStateType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void requestRepaint() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void markAsDirty() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestRepaintAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void markAsDirtyRecursive() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAttached() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void detach() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Extension> getExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeExtension(Extension extension) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeClientResponse(boolean initial) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JsonObject encodeState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean handleConnectorRequest(VaadinRequest request,
			VaadinResponse response, String path) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ServerRpcManager<?> getRpcManager(String rpcInterfaceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ErrorHandler getErrorHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setErrorHandler(ErrorHandler errorHandler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getConnectorId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Unit getWidthUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Unit getHeightUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHeight(String height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWidth(float width, Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeight(float height, Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWidth(String width) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSizeFull() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSizeUndefined() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWidthUndefined() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeightUndefined() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStyleName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStyleName(String style) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addStyleName(String style) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeStyleName(String style) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPrimaryStyleName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPrimaryStyleName(String style) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(HasComponents parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HasComponents getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getCaption() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCaption(String caption) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Resource getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIcon(Resource icon) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UI getUI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void attach() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void readDesign(Element design, DesignContext designContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeDesign(Element design, DesignContext designContext) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addListener(Listener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeListener(Listener listener) {
		// TODO Auto-generated method stub
		
	}
	
	
}
