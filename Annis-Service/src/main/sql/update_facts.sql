UPDATE facts_:id
SET
is_token=(CASE WHEN token_index IS NOT NULL THEN true ELSE false END),
sample_n=(CASE WHEN n_rownum = 1 THEN true ELSE false END),
sample_n_na=(CASE WHEN n_na_rownum = 1 THEN true ELSE false END),
sample_n_r_c=(CASE WHEN n_r_c_rownum = 1 THEN true ELSE false END),
sample_n_r_c_ea=(CASE WHEN n_r_c_ea_rownum = 1 THEN true ELSE false END),
sample_n_r_c_na=(CASE WHEN n_r_c_na_rownum = 1 THEN true ELSE false END)
;

-- remove the obsolete columns
ALTER TABLE facts_:id DROP COLUMN n_rownum;
ALTER TABLE facts_:id DROP COLUMN n_na_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_ea_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_na_rownum;
