----- needed for getNodeAttributeSet
CREATE INDEX idx__node_annotation__value ON node_annotation(name,value,namespace);
CREATE INDEX idx__node_annotation__namespace ON node_annotation(name,namespace);

CREATE INDEX idx__node_token_index ON node (token_index) WHERE token_index IS NOT NULL;
CREATE INDEX idx__node_span ON node (span,toplevel_corpus);
