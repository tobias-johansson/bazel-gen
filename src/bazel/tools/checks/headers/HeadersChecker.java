package tools.checks.headers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class HeadersChecker
{
    public static void main( String[] args )
    {
        byte[] expected = load( "tools/checks/headers/header.java.public.txt" );

        List<Path> failures = Stream.of( args )
                                    .map( Paths::get )
                                    .filter( p -> isFailure( expected, p ) )
                                    .collect( Collectors.toList() );

        if ( failures.isEmpty() )
        {
            System.exit( 0 );
        }
        else
        {
            failures.forEach( System.out::println );
            System.exit( 1 );
        }
    }

    static byte[] load( String resourceName )
    {
        try
        {
            URL resource = HeadersChecker.class.getClassLoader().getResource( resourceName );
            return resource.openStream().readAllBytes();
        }
        catch ( Exception e )
        {
            System.out.println( "Failed to read " + resourceName + " (" + e.getMessage() + ")" );
            System.exit( 1 );
        }
        return null;
    }

    static boolean isFailure( byte[] expected, Path p )
    {
        try
        {
            var length = expected.length;
            var buffer = new byte[length];
            new FileInputStream( p.toFile() ).read( buffer, 0, length );
            return Arrays.compare( buffer, expected ) != 0;
        }
        catch ( IOException e )
        {
            System.out.println( e.getMessage() );
            return true;
        }
    }

    static void showDir( String indent, Path dir )
    {
        System.out.println( indent + dir );
        if ( dir.toFile().isDirectory() )
        {
            try
            {
                Files.newDirectoryStream( dir ).forEach( f -> showDir( indent, f ) );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }
}
