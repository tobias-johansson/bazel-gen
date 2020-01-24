package org.neo4j;

import org.apache.maven.model.Dependency;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.model.Dependencies;
import org.neo4j.model.Global;
import org.neo4j.model.Module;
import org.neo4j.tools.Files;
import org.neo4j.tools.Locations;
import org.neo4j.tools.XmlTools;

@Mojo(
        name = "bazel-generate",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
public class BazelGenerateMojo extends AbstractMojo
{
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    MavenProject project;

    @Parameter( required = true, property = "rootDir" )
    String rootDir;

    public void execute() throws MojoExecutionException
    {
        Global global = new Global( new Files( rootDir ) );
        Module module = new Module( project, global );

        List<Dependencies.Dep> jars = module.dependencies()
                                            .filter( Dependencies.Dep::isJarOrTestJar )
                                            .sorted( Comparator.comparing( Dependencies.Dep::isExternal ) )
                                            .collect( Collectors.toList() );

        JtwigModel model = JtwigModel.newModel()
                                     .with( "module", module )
                                     .with( "compile", jars.stream()
                                                           .filter( Dependencies.Dep::isCompile )
                                                           .map( Dependencies.Dep::target )
                                                           .collect( Collectors.toList() ) )
                                     .with( "test", jars.stream()
                                                        .filter( Dependencies.Dep::isTest )
                                                        .map( Dependencies.Dep::target )
                                                        .collect( Collectors.toList() ) )
                                     .with( "compilerArgs", XmlTools
                                             .start( () -> (Xpp3Dom) project
                                                     .getPlugin( "org.apache.maven.plugins:maven-compiler-plugin" )
                                                     .getConfiguration() )
                                             .child( "compilerArgs" )
                                             .children()
                                             .value()
                                             .collect( Collectors.toList() ) );

        Path file = Paths.get( project.getBasedir().getAbsolutePath() ).resolve( "BUILD" );
        try ( FileOutputStream out = new FileOutputStream( file.toFile() ) )
        {
            JtwigTemplate.classpathTemplate( "templates/BUILD.twig" )
                         .render( model, out );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
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

    <T> T log( T t )
    {
        System.out.println( t );
        return t;
    }
}
