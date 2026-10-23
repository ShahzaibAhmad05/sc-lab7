package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterTest {

    /*
     * Testing Strategy for writtenBy:
     * - Partition: 0 matching tweets, multiple matching tweets, case-insensitive username.
     * 
     * Testing Strategy for inTimespan:
     * - Partition: some matching tweets within range.
     * 
     * Testing Strategy for containing:
     * - Partition: case-insensitive word matching, multiple search words.
     */
    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    private static final Instant d3 = Instant.parse("2016-02-17T12:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    private static final Tweet tweet3 = new Tweet(3, "ALYSSA", "another tweet by alyssa", d3);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testWrittenByMultipleTweetsMultipleResultsCaseInsensitive() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2, tweet3), "alyssa");
        
        assertEquals("expected 2 tweets", 2, writtenBy.size());
        assertTrue("expected list to contain tweet1", writtenBy.contains(tweet1));
        assertTrue("expected list to contain tweet3", writtenBy.contains(tweet3));
    }
    
    @Test
    public void testWrittenByNoMatch() {
        List<Tweet> writtenBy = Filter.writtenBy(Arrays.asList(tweet1, tweet2), "unknown");
        assertTrue("expected empty list", writtenBy.isEmpty());
    }
    
    @Test
    public void testInTimespanMultipleTweetsMultipleResults() {
        Instant testStart = Instant.parse("2016-02-17T09:00:00Z");
        Instant testEnd = Instant.parse("2016-02-17T11:30:00Z");
        
        List<Tweet> inTimespan = Filter.inTimespan(Arrays.asList(tweet1, tweet2, tweet3), new Timespan(testStart, testEnd));
        
        assertEquals("expected 2 tweets", 2, inTimespan.size());
        assertTrue("expected tweet1", inTimespan.contains(tweet1));
        assertTrue("expected tweet2", inTimespan.contains(tweet2));
        assertFalse("excluded tweet3", inTimespan.contains(tweet3));
    }
    
    @Test
    public void testContaining() {
        List<Tweet> containing = Filter.containing(Arrays.asList(tweet1, tweet2, tweet3), Arrays.asList("talk", "HYPE"));
        
        assertEquals("expected 2 tweets", 2, containing.size());
        assertTrue("expected tweet1", containing.contains(tweet1));
        assertTrue("expected tweet2", containing.contains(tweet2));
    }
}