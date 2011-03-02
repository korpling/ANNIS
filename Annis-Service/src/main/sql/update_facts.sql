BEGIN; -- Transaction
UPDATE facts_:id SET is_token=true WHERE token_index IS NOT NULL;

-- select the samples from the partition row number
UPDATE facts_:id SET sample_n=true
WHERE n_rownum = 1;

UPDATE facts_:id SET sample_n_na=true
WHERE n_na_rownum = 1;

UPDATE facts_:id SET sample_n_r_c=true
WHERE n_r_c_rownum = 1;

UPDATE facts_:id SET sample_n_r_c_ea=true
WHERE n_r_c_ea_rownum = 1;

UPDATE facts_:id SET sample_n_r_c_na=true
WHERE n_r_c_na_rownum = 1;

END; -- Transaction

-- remove the obsolete columns
ALTER TABLE facts_:id DROP COLUMN n_rownum;
ALTER TABLE facts_:id DROP COLUMN n_na_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_ea_rownum;
ALTER TABLE facts_:id DROP COLUMN n_r_c_na_rownum;
