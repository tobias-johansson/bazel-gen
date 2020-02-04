package org.neo4j.tools;

import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class XmlTools
{
    private final Stream<Xpp3Dom> elements;

    public static XmlTools start( Supplier<Xpp3Dom> supplier )
    {
        try
        {
            return new XmlTools( supplier.get() );
        }
        catch ( Exception e )
        {
            return new XmlTools( Stream.empty() );
        }
    }

    public XmlTools( Xpp3Dom element )
    {
        this( Stream.of( element ) );
    }

    public XmlTools( Stream<Xpp3Dom> elements )
    {
        this.elements = elements.filter( Objects::nonNull );
    }

    public XmlTools child( String name )
    {
        return map( e -> e.getChild( name ) );
    }

    public XmlTools children()
    {
        return flatMap( e -> Arrays.stream( e.getChildren() ) );
    }

    public Stream<String> value()
    {
        return elements.map( Xpp3Dom::getValue );
    }

    public XmlTools map( Function<Xpp3Dom,Xpp3Dom> function )
    {
        return new XmlTools( elements.map( function ) );
    }

    public XmlTools flatMap( Function<Xpp3Dom,Stream<Xpp3Dom>> function )
    {
        return new XmlTools( elements.flatMap( function ) );
    }
}
