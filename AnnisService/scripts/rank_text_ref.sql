-- create a table that contains pre and post as well as text_ref values for elements to speed up 2nd query
CREATE TABLE rank_text_ref AS 
SELECT rank.*, struct.text_ref FROM rank, struct WHERE rank.struct_ref = struct.id;
