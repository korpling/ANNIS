UPDATE facts_:id
SET
is_token=(CASE WHEN token_index IS NOT NULL THEN true ELSE false END),
sample = 
  ((n_rownum = 1)::integer::bit(5) << 4)
  |  ((n_na_rownum = 1)::integer::bit(5) << 3)
  |  ((n_r_c_rownum = 1)::integer::bit(5) << 2)
  |  ((n_r_c_ea_rownum = 1)::integer::bit(5) << 1 )
  |  ((n_r_c_na_rownum = 1)::integer::bit(5) )
;

-- remove the obsolete columns
ALTER TABLE facts_:id DROP COLUMN n_rownum;
ALTER TABLE facts_:id DROP COLUMN n_na_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_ea_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_na_rownum;
