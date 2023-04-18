package testing;

import edu.yu.da.WeAreAllConnected;
import edu.yu.da.WeAreAllConnected.Segment;
import edu.yu.da.WeAreAllConnectedBase;
import edu.yu.da.WeAreAllConnectedBase.SegmentBase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllConnectedTest {

    private final WeAreAllConnectedBase slimeMold;

    public AllConnectedTest() {
        slimeMold = new WeAreAllConnected();
    }

    // Map 1 is a hub network, where each spoke is further away than the previous
    // Because nothing connects the outer rim, each added segment will only affect what it connects
    // the best possible addition is 8-9, assuming all additions are equal speed
    private List<SegmentBase> makeHubMap() {
        List<SegmentBase> map = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            map.add(new Segment(0, i, i));
        }
        return map;
    }

    // test that when given a single possibility, it chooses that possibility
    @Test
    public void oneChoiceTest() {
        SegmentBase rightSegment = new Segment(1, 2, 1);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment);
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    // test that when given multiple possibilities, but one is better than the other, it chooses that
    @Test
    public void bestChoiceTest() {
        SegmentBase rightSegment = new Segment(8, 9, 1);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment, new Segment(1, 2, 1));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    // test that when given multiple possibilities, 2 of which tie for best, it chooses one of the tied segments
    @Test
    public void bestChoiceTiedTest() {
        SegmentBase rightSegment = new Segment(8, 9, 2);
        SegmentBase alsoRightSegment = new Segment(7, 9, 1);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment, alsoRightSegment, new Segment(1, 2, 1));
        SegmentBase returnValue = slimeMold.findBest(10, map, additions);
        if (!(returnValue.equals(rightSegment) || returnValue.equals(alsoRightSegment))) {
            fail(returnValue + " is not " + rightSegment + " or " + alsoRightSegment);
        }
    }

    // test where most expensive possibility is best
    @Test
    public void expensiveIsBestTest() {
        SegmentBase rightSegment = new Segment(8, 9, 5);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment, new Segment(1, 2, 1));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    // test where there are more choices than roads on the map
    @Test
    public void manyChoicesTest() {
        SegmentBase rightSegment = new Segment(1, 9, 1);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment, new Segment(1, 2, 1),
                new Segment(2, 3, 1), new Segment(3, 4, 1),
                new Segment(4, 5, 1), new Segment(5, 6, 1),
                new Segment(6, 7, 1), new Segment(7, 8, 1),
                new Segment(9, 8, 1), new Segment(1, 3, 1),
                new Segment(1, 4, 2));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    @Test
    public void denseGraphTest() {
        List<SegmentBase> map = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                map.add(new Segment(i, j, 2));
            }
        }
        SegmentBase rightSegment = new Segment(0, 1, 1);

        assertEquals(rightSegment, slimeMold.findBest(10, map, List.of(rightSegment)));
    }

    // test with a more scattered network
    /* Additions: 1->4 (4) makes 1->4 from 7 to 4
    It makes 0->4 from 8 to 5
    That is a total benefit of 6, not what I expected, mostly because it is expensive

    Let's add a hyperspace rail of 6->9 (2)
    It makes 6->9 from 9 to 2, +7
    It makes 5->9 from 7 to 4, +3
    It makes 0->9 from 10 to 7, +3
    It changes 1->9 in the same way, +3
    It makes 2->9 from 12 to 9, +3
    It makes 3->9 from 12 to 11, +1
    It makes 6->8 from 6 to 5, +1
    So, that's a total of 21

    Let's add a new local quickrail from 2-0 (1)
    It makes 2-0 from 3 to 1, +2
    It makes 3-0 from 5 to 3, +2
    It makes 4-0 from 8 to 6, +2
    It makes 2->5 from 5 to 4, +1
    It makes 3->5 from 7 to 6, +1
    It makes 2->6 from 7 to 6, +1
    It makes 3->6 from 9 to 8, +1
    It makes 2->7 from 7 to 6, +1
    It likewise improves 2->8 and 2->9 by 1 each, +2
    So, that's a total of 13

    Now let's add something no one will ever use, our train to nowhere. 9-7 (5). Nice and direct, but no improvement
    * */
    @Test
    public void scatteredMapTest() {
        SegmentBase rightSegment = new Segment(6, 9, 2);
        List<SegmentBase> map = List.of(    new Segment(0, 1, 1),
                new Segment(0, 5, 3), new Segment(1, 2, 2),
                new Segment(1, 5, 3), new Segment(2, 3, 2),
                new Segment(3, 4, 3), new Segment(4, 7, 4),
                new Segment(5, 6, 2), new Segment(5, 7, 2),
                new Segment(7, 8, 2), new Segment(8, 9, 3)    );
        List<SegmentBase> additions = List.of(rightSegment, new Segment(1, 4, 4),
                new Segment(0, 2, 1), new Segment(9, 7, 5));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    // test where no suggestion improves total duration
    // like, in our wheel, we add 1-2 (3), 2-4 (7), 5-6 (12), and 7-9 (16)
    @Test
    public void noGoodChoiceTest() {
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(new Segment(1, 2, 3),
                new Segment(2, 4, 7), new Segment(5, 6, 12),
                new Segment(7, 9, 16));
        assertNull(slimeMold.findBest(10, map, additions));
    }

    // test where there are multiple segments in possibilities that link the same location
    // but with different durations
    @Test
    public void multipleVersionsTest() {
        SegmentBase rightSegment = new Segment(8, 9, 1);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment, new Segment(8, 9, 2));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    // tests that it accepts a shorter replacement when it is more of an improvement than a different choice
    @Test
    public void shorterReplacementTest() {
        SegmentBase rightSegment = new Segment(0, 9, 1);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment, new Segment(1, 2, 1));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    // tests that when the replacement is the same as a vertex already there, it returns null
    @Test
    public void equalLengthReplacementTest() {
        SegmentBase rightSegment = new Segment(0, 9, 9);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment);
        assertNull(slimeMold.findBest(10, map, additions));
    }

    // same as above, but there is a different solution that does improve
    @Test
    public void equalLengthReplacementIgnoredWhenBetterTest() {
        SegmentBase rightSegment = new Segment(1, 2, 1);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment, new Segment(0, 9, 9));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }

    // test that when we have an inferior replacement, it is ignored
    @Test
    public void longReplacementTest() {
        SegmentBase rightSegment = new Segment(0, 9, 20);
        List<SegmentBase> map = makeHubMap();
        List<SegmentBase> additions = List.of(rightSegment);
        assertNull(slimeMold.findBest(10, map, additions));
    }

    // test where works when not given my custom subclass
    @Test
    public void otherSubclassTest() {
        SegmentBase rightSegment = new SegmentBase(8, 9, 1);
        List<SegmentBase> map = new ArrayList<>();
        for (int i = 1; i < 10; i++) {
            map.add(new SegmentBase(0, i, i));
        }
        List<SegmentBase> additions = List.of(rightSegment, new SegmentBase(1, 2, 1));
        assertEquals(rightSegment, slimeMold.findBest(10, map, additions));
    }


}
