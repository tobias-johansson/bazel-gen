package org.neo4j;

import com.google.common.collect.Lists;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(
        name = "bazel-generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class BazelGenerate extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    MavenProject project;

    Path root = Paths.get( "/home/tobias/Projects/neo4j" );

    Locations locations = new Locations( root.resolve( "module-paths.txt" ).toFile() );

    public BazelGenerate() throws IOException
    {
    }

    public void execute() throws MojoExecutionException
    {

        project.getDependencies().forEach( this::println );

        List<String> compile_int = project.getDependencies().stream()
                .filter( d -> !d.getScope().equals( "test" ) )
                .filter( d -> d.getVersion().equals( "4.0.0-SNAPSHOT" ) )
                .map( this::toLocalTarget ).collect( Collectors.toList() );

        List<String> compile_ext = project.getDependencies().stream()
                .filter( d -> !d.getScope().equals( "test" ) )
                .filter( d -> !d.getVersion().equals( "4.0.0-SNAPSHOT" ) )
                .map( this::toExternalTarget ).collect( Collectors.toList() );

        List<String> test_int = project.getDependencies().stream()
                .filter( d -> d.getScope().equals( "test" ) )
                .filter( d -> d.getVersion().equals( "4.0.0-SNAPSHOT" ) )
                .map( this::toLocalTarget ).collect( Collectors.toList() );

        List<String> test_ext = project.getDependencies().stream()
                .filter( d -> d.getScope().equals( "test" ) )
                .filter( d -> !d.getVersion().equals( "4.0.0-SNAPSHOT" ) )
                .map( this::toExternalTarget ).collect( Collectors.toList() );

        List<String> compile = new ArrayList<>();
        compile.addAll( compile_int );
        compile.addAll( compile_ext );

        List<String> test = new ArrayList<>();
        test.addAll( test_int );
        test.addAll( test_ext );

        Plugin plugin = project.getPlugin( "org.apache.maven.plugins:maven-compiler-plugin" );
        List<String> compilerArgs = Collections.emptyList();
        if ( plugin != null )
        {
            try
            {
                Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();

                compilerArgs = Arrays.stream( configuration.getChild( "compilerArgs" ).getChildren() )
                        .map( Xpp3Dom::getValue )
                        .collect( Collectors.toList() );
            }
            catch ( NullPointerException e )
            {
            }
        }

        JtwigTemplate template = JtwigTemplate.classpathTemplate( "templates/BUILD.twig" );
        JtwigModel model = JtwigModel.newModel()
                .with( "compile", compile )
                .with( "test", test )
                .with( "compilerArgs", compilerArgs );

        Path file = Paths.get( project.getBasedir().getAbsolutePath() ).resolve( "BUILD" );
        try ( FileOutputStream out = new FileOutputStream( file.toFile() ) )
        {
            template.render( model, out );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    private String toLocalTarget( Dependency d )
    {
        String id = d.getGroupId() + ":" + d.getArtifactId();
        String loc = locations.map.get( id );
        return "//" + loc + ":" + d.getType();
    }

    private String toExternalTarget( Dependency d )
    {
        String id = d.getGroupId() + ":" + d.getArtifactId();
        return "@maven//:" + id.replaceAll( "[^a-zA-Z0-9]", "_" );
    }

    void println( Dependency d )
    {
        System.out.println( d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion() + " @ " + d.getScope() );
    }

    Dependency log( Dependency d )
    {
        println( d );
        return d;
    }
}
