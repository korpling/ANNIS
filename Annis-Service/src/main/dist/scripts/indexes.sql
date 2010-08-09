----- needed for getNodeAttributeSet
CREATE INDEX node_annotation_namespace ON node_annotation(namespace,name,value);

CREATE INDEX idx__node_token_index ON node (token_index) WHERE token_index IS NOT NULL;
CREATE INDEX idx__node_span ON node (span,toplevel_corpus);
