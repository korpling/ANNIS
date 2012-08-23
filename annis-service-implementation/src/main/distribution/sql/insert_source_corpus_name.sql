ALTER TABLE _corpus ADD COLUMN source_path varchar;

UPDATE corpus SET source_path = ':path' 
WHERE top_level = true AND id = :id;