package org.corpus_tools.annis;

import com.vaadin.spring.internal.BeanStore;
import com.vaadin.spring.internal.BeanStoreRetrievalStrategy;

/**
 * Singleton bean store retrieval strategy that always returns the same bean store and conversation
 * id. This strategy is primarily a helper for testing.
 * <p>
 * Copied from
 * https://github.com/vaadin/spring/blob/3.0/vaadin-spring/src/test/java/com/vaadin/spring/test/util/SingletonBeanStoreRetrievalStrategy.java
 */
public class SingletonBeanStoreRetrievalStrategy implements BeanStoreRetrievalStrategy {

    public static final String CONVERSATION_ID = "testConversation";
    private BeanStore beanStore = new BeanStore("testBeanStore");

    @Override
    public BeanStore getBeanStore() {
        return beanStore;
    }

    @Override
    public String getConversationId() {
        return CONVERSATION_ID;
    }
}
