-- create a table that contains pre and post as well as text_ref values for elements to speed up 2nd query
CREATE TABLE rank_text_ref AS 
SELECT edges.*, struct.text_ref FROM edges, node AS struct WHERE edges.node_ref = struct.id;
