CREATE INDEX idx__cluster__:id ON facts_:id(n_sample, corpus_ref, is_token);

CLUSTER facts_:id USING idx__cluster__:id;

--DROP INDEX idx__cluster__:id;