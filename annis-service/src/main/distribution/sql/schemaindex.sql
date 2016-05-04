CREATE INDEX idx__corpus__pre_post ON corpus (pre,post);
CREATE INDEX idx__corpus__name ON corpus ("name");
CREATE INDEX idx__corpus__tlc ON corpus (top_level);
CREATE INDEX idx__corpus__tlcname ON corpus ("name") WHERE top_level IS TRUE;
CREATE INDEX idx__corpus__prepostid ON corpus(pre, post, id);


CREATE INDEX idx__annocat__name ON annotation_category("name");
CREATE INDEX idx__annocat__namespace ON annotation_category("namespace");
CREATE INDEX idx__annocat__corpus ON annotation_category("toplevel_corpus");

CREATE INDEX idx__annocat__namebyid ON annotation_category(id, toplevel_corpus, "name");
CREATE INDEX idx__annocat__namespacebyid ON annotation_category(id, toplevel_corpus, "namespace");

CREATE INDEX idx__urlshortener__url ON url_shortener(url, id);