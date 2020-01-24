package org.neo4j.model;

import org.neo4j.tools.Files;
import org.neo4j.tools.Locations;

public class Global
{
    public final Locations locations;
    public final Files files;
    public final Dependencies dependencies;

    public Global( Files files )
    {
        this.files = files;
        this.locations = new Locations( files );
        this.dependencies = new Dependencies( locations );
    }
}
