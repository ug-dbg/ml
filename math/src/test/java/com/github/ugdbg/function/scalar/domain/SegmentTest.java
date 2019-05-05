package com.github.ugdbg.function.scalar.domain;

import com.github.ugdbg.function.domain.Domain;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test case for the {@link Segment} domain.
 */
public class SegmentTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadSegmentConstructorLeft() {
		new Segment(null, 0F);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadSegmentConstructorRight() {
		new Segment(0f, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadSegmentConstructor() {
		new Segment(null, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadIsIn() {
		new Segment(0f, 1f).isIn(null);
	}
	
	@Test
	public void testBadEquals() {
		Assert.assertNotEquals(new Segment(0f, 1f), "String");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testBadInter() {
		new Segment(0f, 1f).inter(new Domain<Float>() {
			@Override
			public boolean isIn(Float x) {
				return false;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public Domain<Float> inter(Domain other) {
				return null;
			}

			@Override
			public int compareTo(Domain o) {
				return 0;
			}

			@Override
			public Domain<Float> union(Domain<Float>[] other) {
				return null;
			}
		});
	}
	
	@Test
	public void testEmptySegment() {
		Segment empty = new Segment(1F, 0F);
		Assert.assertTrue(empty.isEmpty());
		Assert.assertEquals("∅", empty.toString());
	
		Assert.assertFalse(empty.isIn(0.5f));
		
		Assert.assertEquals(+1, empty.compareTo(Domains.R));
		Assert.assertEquals(+1, empty.compareTo(Domains.R_PLUS_STAR));
		Assert.assertEquals(+0, empty.compareTo(new Segment(1F, 0F)));
		Assert.assertEquals(-1, empty.compareTo(new Segment(5F, 3F)));
	}
	
	@Test
	public void testOpenSegment() {
		Segment segment = new Segment(0F, 7F);
		Assert.assertFalse(segment.isEmpty());
		Assert.assertEquals("]0.0, 7.0[", segment.toString());
		
		Assert.assertTrue(segment.isIn(0.5f));
		Assert.assertTrue(segment.isIn(5f));
		Assert.assertFalse(segment.isIn(0f));
		Assert.assertFalse(segment.isIn(7f));
		Assert.assertFalse(segment.isIn(8f));
		Assert.assertFalse(segment.isIn(Float.NEGATIVE_INFINITY));
		
		Assert.assertEquals(+1, segment.compareTo(Domains.R));
		Assert.assertEquals(-1, segment.compareTo(Domains.R_PLUS_STAR));
		Assert.assertEquals(+0, segment.compareTo(new Segment(0F, 7F)));
		Assert.assertEquals(+1, segment.compareTo(new Segment(0F, 6F)));
		Assert.assertEquals(-1, segment.compareTo(new Segment(0F, 7F).open(true,  false)));
		Assert.assertEquals(+1, segment.compareTo(new Segment(0F, 7F).open(false, false)));
		Assert.assertEquals(-1, segment.compareTo(new Segment(5F, 3F)));
	}
	
	@Test
	public void testClosedSegment() {
		Segment segment = new Segment(0F, 7F).open(false, false);
		Assert.assertFalse(segment.isEmpty());
		Assert.assertEquals("[0.0, 7.0]", segment.toString());
		
		Assert.assertTrue(segment.isIn(0.5f));
		Assert.assertTrue(segment.isIn(5f));
		Assert.assertTrue(segment.isIn(0f));
		Assert.assertTrue(segment.isIn(7f));
		Assert.assertFalse(segment.isIn(8f));
		
		Assert.assertEquals(+1, segment.compareTo(Domains.R));
		Assert.assertEquals(-1, segment.compareTo(Domains.R_PLUS_STAR));
		Assert.assertEquals(+0, segment.compareTo(new Segment(0F, 7F).open(false, false)));
		Assert.assertEquals(+1, segment.compareTo(new Segment(0F, 6F).open(false, false)));
		Assert.assertEquals(-1, segment.compareTo(new Segment(0F, 7F).open(true,  false)));
		Assert.assertEquals(+1, segment.compareTo(new Segment(0F, 7F).open(false, true)));
		Assert.assertEquals(-1, segment.compareTo(new Segment(5F, 3F)));
	}
	
	@Test
	public void testDisjunctSegmentInter() {
		Segment segment = new Segment(0F, 7F);
		Segment other   = new Segment(9F, 17F);
		Segment intersection = segment.inter(other);
		Assert.assertTrue(intersection.isEmpty());
		Assert.assertFalse(intersection.isIn(0f));
		Assert.assertFalse(intersection.isIn(7f));
		Assert.assertFalse(intersection.isIn(8f));
		Assert.assertFalse(intersection.isIn(Float.POSITIVE_INFINITY));
		Assert.assertEquals("∅", intersection.toString());
	}
	
	@Test
	public void testNonDisjunctSegmentInter() {
		Segment segment = new Segment(0F, 7F);
		Segment other   = new Segment(5F, 17F);
		Segment intersection = segment.inter(other);
		Assert.assertFalse(intersection.isEmpty());
		
		Assert.assertFalse(intersection.isIn(0f));
		Assert.assertTrue(intersection.isIn(6f));
		Assert.assertFalse(intersection.isIn(7f));
		Assert.assertFalse(intersection.isIn(8f));
		Assert.assertFalse(intersection.isIn(Float.POSITIVE_INFINITY));
		
		Assert.assertEquals("]5.0, 7.0[", intersection.toString());
	}
	
	@Test
	public void testSegmentInterWithR() {
		Segment segment = new Segment(0F, 7F);
		Domain<Float>  other = Domains.R;
		Domain<Float> intersection = segment.inter(other);
		Assert.assertFalse(intersection.isEmpty());
		Assert.assertEquals(intersection, segment);
		
		Assert.assertFalse(intersection.isIn(0f));
		Assert.assertTrue(intersection.isIn(6f));
		Assert.assertFalse(intersection.isIn(7f));
		Assert.assertFalse(intersection.isIn(8f));
	}
	
	@Test
	public void testSegmentInMap() {
		Segment segment0to7 = new Segment(0F, 7F);
		Segment segment2to7 = new Segment(2F, 7F);
		Segment segment2Closedto7 = new Segment(2F, 7F).open(false, true);

		Map<Segment, String> segmentMap = new HashMap<>();
		segmentMap.put(segment0to7, segment0to7.toString());
		segmentMap.put(segment2to7, segment2to7.toString());
		segmentMap.put(segment2Closedto7, segment2Closedto7.toString());
		
		Assert.assertEquals(3, segmentMap.size());
		Assert.assertEquals(segment2to7.toString(), segmentMap.get(segment2to7));
		
		segmentMap.put(new Segment(2F, 7F).open(false, true), "Modified");
		Assert.assertEquals("Modified", segmentMap.get(segment2Closedto7));
	}
}
