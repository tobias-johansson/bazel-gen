package org.neo4j.tools;

import com.opencsv.CSVReader;
import org.apache.maven.project.MavenProject;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Locations
{
    public final Files files;
    public final Path locationsFile;
    public Map<String,String> locations = new HashMap<>();

    public Locations( Files files )
    {
        this.files = files;
        this.locationsFile = files.tmp.resolve( "module-locations.csv" );

        try ( CSVReader csvReader = new CSVReader( new FileReader( locationsFile.toFile() ) ) )
        {
            String[] values;
            while ( (values = csvReader.readNext()) != null )
            {
                String module = values[0];
                String location = values[1];
                locations.put( module, location );
            }
        }
        catch ( Exception e )
        {
            System.out.println( "Unable to read module locations" );
        }
    }

    public void add( MavenProject project )
    {
        Path base = Paths.get( project.getBasedir().getAbsolutePath() );
        Path relative = files.root.relativize( base );
        String key = key( project.getGroupId(), project.getArtifactId() );
        String line = "\"" + key + "\"" + ", " + "\"" + relative.toString() + "\"";

        try ( FileOutputStream os = new FileOutputStream( locationsFile.toFile(), true ); PrintStream ps = new PrintStream( os ) )
        {
            ps.println( line );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private String key( String groupId, String artifactId )
    {
        return groupId + ":" + artifactId;
    }

    public String get( String groupId, String artifactId )
    {
        return locations.get( key( groupId, artifactId ) );
    }
}
