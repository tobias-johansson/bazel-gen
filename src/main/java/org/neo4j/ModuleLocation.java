package org.neo4j;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(
        name = "module-location",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class ModuleLocation extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    MavenProject project;

    Path root = Paths.get( "/home/tobias/Projects/neo4j" );

    public void execute() throws MojoExecutionException
    {
        printPomPath();
    }

    private void printPomPath()
    {
        Path base = Paths.get( project.getBasedir().getAbsolutePath() );
        Path relative = root.relativize( base );
        String name = project.getGroupId() + ":" + project.getArtifactId();
        String line = "\"" + name + "\"" + ", " + "\"" + relative.toString() + "\"";
        System.out.println( line );
        File out = root.resolve( "module-paths.txt" ).toFile();
        try ( FileOutputStream os = new FileOutputStream( out, true ); PrintStream ps = new PrintStream( os ) )
        {
            ps.println( line );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }
}
