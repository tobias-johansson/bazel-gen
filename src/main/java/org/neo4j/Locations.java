package org.neo4j;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Locations
{
    public Map<String,String> map = new HashMap<>();

    public Locations( File moduleLocations ) throws IOException
    {
        try ( CSVReader csvReader = new CSVReader( new FileReader( moduleLocations ) ); )
        {
            String[] values = null;
            while ( (values = csvReader.readNext()) != null )
            {
                String module = values[0];
                String location = values[1];
                map.put( module, location );
            }
        }
    }
}
