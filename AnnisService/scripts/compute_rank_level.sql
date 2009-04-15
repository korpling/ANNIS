-- UPDATE _rank SET level = 0 WHERE parent IS NULL;
-- UPDATE _rank SET LEVEL = 1 WHERE parent IN (SELECT pre FROM _rank WHERE level = 0);

CREATE OR REPLACE FUNCTION compute_rank_level() RETURNS VOID
AS $$
    # plan to set level for tep-level _rank entries
    plan_start = plpy.prepare('UPDATE _rank SET level = $1 WHERE parent IS NULL', [ "int" ])
    
    # plan to get number of entries with level unset
    plan_unset_count = plpy.prepare('SELECT count(*) AS count FROM _rank WHERE level IS NULL')
    
    # plan to set level for entries which parents have a level
    plan_induction = plpy.prepare('UPDATE _rank r1 SET LEVEL = $1 FROM _rank r2 WHERE r1.parent = r2.pre and r2.level = $2;', [ "int", "int" ] )
    
    # set the level of top-level entries
    plpy.notice("setting rank level for roots")
    plpy.execute(plan_start, [ 0 ] )

    # loop...
    i = 0
    while True:
        i += 1
    
        # get # of entries where level is unset
        count = plpy.execute(plan_unset_count)[0]["count"]
        plpy.notice("%d rank entries without level" % (count, ) )
        
        # break if no entries with level = NULL
        if count == 0:
            break
            
        # set level for next entries
        plpy.notice("setting rank levels %d" % (i, ) )
        plpy.execute(plan_induction, [i, i - 1])

    plpy.notice("iterations: %d" % (i - 1, ) )

    return None
$$ language plpythonu;
