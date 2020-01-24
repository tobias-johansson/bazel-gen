package org.neo4j.tools;

import com.opencsv.CSVReader;
import org.apache.maven.project.MavenProject;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Locations
{
    public final Files files;
    public final Path moduleLocations;
    public Map<String,String> map = new HashMap<>();

    public Locations( Files files )
    {
        this.files = files;
        this.moduleLocations = files.tmp.resolve( "module-locations.csv" );

        try ( CSVReader csvReader = new CSVReader( new FileReader( moduleLocations.toFile() ) ) )
        {
            String[] values;
            while ( (values = csvReader.readNext()) != null )
            {
                String module = values[0];
                String location = values[1];
                map.put( module, location );
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Unable to read module locations" );
        }
    }
}
