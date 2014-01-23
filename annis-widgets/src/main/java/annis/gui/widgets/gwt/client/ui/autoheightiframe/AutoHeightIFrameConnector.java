/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.widgets.gwt.client.ui.autoheightiframe;

import annis.gui.widgets.AutoHeightIFrame;
import annis.gui.widgets.gwt.client.ui.VAutoHeightIFrame;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Connect(AutoHeightIFrame.class)
public class AutoHeightIFrameConnector extends AbstractComponentConnector
{

  private AutoHeightIFrameServerRpc rpc = 
    RpcProxy.create(AutoHeightIFrameServerRpc.class, this);
  
  public AutoHeightIFrameConnector()
  {
    getWidget().setLoadCallback(new LoadCallback() 
    {
      @Override
      public void onIFrameLoaded(float newHeight)
      {
        rpc.newHeight(newHeight);
      }
    });
  }
  
  @Override
  public VAutoHeightIFrame getWidget()
  {
    return (VAutoHeightIFrame) super.getWidget();
  }

  @Override
  public AutoHeightIFrameState getState()
  {
    return (AutoHeightIFrameState) super.getState();
  }

  @Override
  public void onStateChanged(StateChangeEvent stateChangeEvent)
  {
    super.onStateChanged(stateChangeEvent);
    getWidget().update(getState().getUrl(), getState().getAdditionalHeight());
  }
  
  public interface LoadCallback
  {
    public void onIFrameLoaded(float newHeight);
  }
  
}
