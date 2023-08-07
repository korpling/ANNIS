package org.corpus_tools.annis.gui.controller;

import com.vaadin.ui.UI;
import java.util.LinkedList;
import java.util.List;
import org.corpus_tools.annis.api.model.QueryAttributeDescription;
import org.corpus_tools.annis.gui.controlpanel.QueryPanel;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class ValidateCallback implements Subscriber<QueryAttributeDescription> {

  private List<QueryAttributeDescription> result;

  private final UI ui;
  private final QueryPanel qp;
  private final QueryController controller;

  public ValidateCallback(QueryPanel qp, QueryController controller, UI ui) {
    this.qp = qp;
    this.controller = controller;
    this.ui = ui;
  }

  @Override
  public void onSubscribe(Subscription s) {
    result = new LinkedList<>();
  }

  @Override
  public void onNext(QueryAttributeDescription attDesc) {
    result.add(attDesc);

  }

  @Override
  public void onError(Throwable t) {
    if (t instanceof WebClientResponseException) {
      controller.reportServiceException((WebClientResponseException) t, false);
    }
  }

  @Override
  public void onComplete() {

    ui.access(() -> {
      qp.setNodes(result);
      if (controller.getState().getSelectedCorpora() == null
          || controller.getState().getSelectedCorpora().isEmpty()) {
        qp.setStatus("Please select a corpus from the list below, then click on \"Search\".");
      } else {
        qp.setStatus("Valid query, click on \"Search\" to start searching.");
      }
    });
  }

}
