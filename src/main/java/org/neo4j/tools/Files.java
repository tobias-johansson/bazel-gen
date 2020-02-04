package org.neo4j.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Files
{
    public final Path root;
    public final Path tmp;

    public Files( String rootDir )
    {
        this( Paths.get( rootDir ) );
    }

    public Files( Path root )
    {
        this.root = root;
        this.tmp = root.resolve( ".bazel-gen" );
        if (!java.nio.file.Files.exists( tmp ))
        {
            try
            {
                java.nio.file.Files.createDirectory(tmp);
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                throw new RuntimeException( e );
            }
        }
    }
}
