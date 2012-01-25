UPDATE _node_annotation SET node_ref = node_ref + (SELECT  max_node_id FROM corpus_stats WHERE id = :id);
    
UPDATE _rank
SET 
 pre = pre + (SELECT max_rank_post FROM corpus_stats WHERE id = :id),
 post = post + (SELECT max_rank_post FROM corpus_stats WHERE id = :id),
 node_ref = node_ref + (SELECT max_node_id FROM corpus_stats WHERE id = :id),
 parent = parent + (SELECT max_rank_post FROM corpus_stats WHERE id = :id),
 component_ref = component_ref + (SELECT max_component_id FROM corpus_stats WHERE id = :id);

UPDATE _component SET id = id + (SELECT max_component_id FROM corpus_stats WHERE id = :id);

UPDATE _edge_annotation SET rank_ref = rank_ref + (SELECT  max_rank_post FROM corpus_stats WHERE id = :id);
    
UPDATE _node
SET 
 id = id + (SELECT max_node_id FROM corpus_stats WHERE id = :id),
 text_ref = text_ref + (SELECT  max_text_id FROM corpus_stats WHERE id = :id),
 corpus_ref = corpus_ref + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id),
 toplevel_corpus = toplevel_corpus + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id);
    
UPDATE _text SET id = id + (SELECT  max_text_id FROM corpus_stats WHERE id = :id);
    
UPDATE _corpus
SET 
 id = id + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id),
 pre = pre + (SELECT max_corpus_post FROM corpus_stats WHERE id = :id),
 post = post + (SELECT max_corpus_post FROM corpus_stats WHERE id = :id);

UPDATE _corpus_annotation SET corpus_ref = corpus_ref + (SELECT max_corpus_id FROM corpus_stats WHERE id = :id);

