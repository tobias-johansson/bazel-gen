package org.neo4j.model;

import org.apache.maven.model.Dependency;

import org.neo4j.tools.Locations;

public class Dependencies
{
    private final Locations locations;

    public Dependencies( Locations locations )
    {
        this.locations = locations;
    }

    public Dep create( Dependency dependency )
    {
        return new Dep( dependency );
    }

    public class Dep
    {
        private final Dependency dependency;

        private Dep( Dependency dependency )
        {
            this.dependency = dependency;
        }

        public String id()
        {
            return dependency.getGroupId() + ":" + dependency.getArtifactId();
        }

        public String coords()
        {
            return id() + ":" + dependency.getVersion();
        }

        public boolean isTest()
        {
            return dependency.getScope().equals( "test" );
        }

        public boolean isJarOrTestJar()
        {
            return dependency.getType().equals( "jar" ) || dependency.getType().equals( "test-jar" );
        }

        public boolean isCompile()
        {
            return !isTest();
        }

        public boolean isInternal()
        {
            return dependency.getVersion().equals( "4.0.1-SNAPSHOT" );
        }

        public boolean isExternal()
        {
            return !isInternal();
        }

        public String target()
        {
            return isInternal() ? internalTarget() : externalTarget();
        }

        private String internalTarget()
        {
            String loc = locations.map.get( id() );
            return "//" + loc + ":" + dependency.getType();
        }

        private String externalTarget()
        {
            return "@maven//:" + id().replaceAll( "[^a-zA-Z0-9]", "_" );
        }
    }
}
