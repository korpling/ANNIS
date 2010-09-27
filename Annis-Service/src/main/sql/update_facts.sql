UPDATE facts_:id SET is_token=true WHERE token_index IS NOT NULL;

-- select the samples from the partition row number
UPDATE facts_:id SET sample_node=true
WHERE node_rownum = 1;

UPDATE facts_:id SET sample_node_annotation=true
WHERE node_anno_rownum = 1;

-- remove the obsolete columns
ALTER TABLE facts_:id DROP COLUMN node_rownum;
ALTER TABLE facts_:id DROP COLUMN node_anno_rownum;
