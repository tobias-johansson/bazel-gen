package org.neo4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BazelGenerateTest extends BetterAbstractMojoTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testSomething() throws Exception
    {
//        File pom = new File("/home/tobias/Projects/neo4j/public/annotations/pom.xml");
        Path proj = Paths.get( "/home/tobias/Projects/neo4j/public/annotations" );
        File pom = proj.resolve( "pom.xml" ).toFile();
        assertNotNull( pom );
        assertTrue( pom.exists() );

        BazelGenerate bazelGenerate = (BazelGenerate) lookupConfiguredMojo( pom, "bazel-generate" );
        assertNotNull( bazelGenerate );
        bazelGenerate.execute();

        File build = proj.resolve( "BUILD" ).toFile();

        try (BufferedReader br = new BufferedReader(new FileReader(build))) {
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}
