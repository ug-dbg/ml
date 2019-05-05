package com.github.ugdbg.function.scalar.domain;

import com.github.ugdbg.function.domain.Domain;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for the {@link Union} domain.
 */
public class UnionTest {
	
	@Test
	public void testDisjunctSegmentUnion() {
		Union union = new Union(new Segment(0f, 1f).open(false, true), new Segment(1f, 2f).open(true, false));
		
		Assert.assertFalse(union.isEmpty());
		Assert.assertFalse(union.isIn(1f));
		Assert.assertTrue(union.isIn(0f));
		Assert.assertTrue(union.isIn(0.5f));
		Assert.assertTrue(union.isIn(1.5f));
		Assert.assertTrue(union.isIn(2f));
		
		Assert.assertEquals("[0.0, 1.0[ ⋃ ]1.0, 2.0]", union.toString());
	}
	
	@Test
	public void testNonDisjunctSegmentUnion() {
		Union union = new Union(new Segment(0f, 2f).open(false, true), new Segment(1f, 3f).open(true, false));
		
		Assert.assertFalse(union.isEmpty());
		Assert.assertTrue(union.isIn(1f));
		Assert.assertTrue(union.isIn(0f));
		Assert.assertTrue(union.isIn(0.5f));
		Assert.assertTrue(union.isIn(1.5f));
		Assert.assertTrue(union.isIn(2f));
		Assert.assertTrue(union.isIn(2.5f));
		Assert.assertTrue(union.isIn(3f));
		
		Assert.assertEquals("[0.0, 2.0[ ⋃ ]1.0, 3.0]", union.toString());
	}
	
	@Test
	public void testUnionWithSelf() {
		Union union = new Union(new Segment(0f, 2f), new Segment(0f, 2f));
		
		Assert.assertEquals(1, union.size());
		
		Assert.assertFalse(union.isEmpty());
		Assert.assertTrue(union.isIn(1f));
		Assert.assertFalse(union.isIn(0f));
		Assert.assertTrue(union.isIn(0.5f));
		Assert.assertTrue(union.isIn(1.5f));
		Assert.assertFalse(union.isIn(2f));
		Assert.assertFalse(union.isIn(3f));
		
		Assert.assertEquals("]0.0, 2.0[", union.toString());
	}
	
	@Test
	public void testCompareUnions() {
		Union union = new Union(new Segment(0f, 2f), new Segment(4f, 5f));
		Union other = new Union(new Segment(55f, 152f).inter(new Segment(12f, 60f), new Segment(55f, 90f)));
		Assert.assertTrue(union.compareTo(other) < 0);
		
		union = new Union();
		Assert.assertNull(union.lowest());
		Assert.assertTrue(union.compareTo(other) > 0);
		
		union = new Union(Domains.R_MINUS_STAR, Domains.R_PLUS_STAR);
		other = new Union(new Segment(-55f, -5f).inter(new Segment(-12f, 60f), new Segment(-7f, 90f)));
		Assert.assertTrue(union.compareTo(other) > 0);

		union = union.union(other);
		Assert.assertEquals("]-7.0, -5.0[", union.lowest().toString());
	}
	
	@Test
	public void testUnionOfUnions() {
		Union union = new Union(new Segment(0f, 2f), new Segment(4f, 5f));
		union = union.union(new Segment(-5f, -3f));
		union = union.union(new Union(new Segment(55f, 152f).inter(new Segment(12f, 60f), new Segment(55f, 90f))));

		Assert.assertEquals("]-5.0, -3.0[ ⋃ ]0.0, 2.0[ ⋃ ]4.0, 5.0[ ⋃ ]55.0, 60.0[", union.toString());
	}
	
	@Test
	public void testEmptyInterUnion() {
		Domain<Float> union = new Union(new Segment(55f, 152f).inter(new Segment(12f, 60f), new Segment(55f, 90f)));
		Assert.assertEquals("]55.0, 60.0[", union.toString());

		union = union.inter(new Union(new Segment(0f, 1f), new Segment(10f, 13f)));
		Assert.assertTrue(union.isEmpty());
		Assert.assertEquals("∅", union.toString());
	}
	
}
