package com.github.ugdbg.function.vector.domain;

import com.github.ugdbg.function.domain.Domain;
import com.github.ugdbg.function.scalar.domain.Domains;
import com.github.ugdbg.function.scalar.domain.Segment;
import com.github.ugdbg.function.vector.Vector;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeSet;

/**
 * Test case for the common {@link VDomain} objects.
 */
public class VDomainTest {
	
	@Test(expected = RuntimeException.class)
	public void test_BadRange_INF() {
		VDomain r5 = VDomains.R(5);
		r5.isIn(Vector.of(-2f, 3f, 154f, 0f));
	}
	
	@Test(expected = RuntimeException.class)
	public void test_BadRange_SUP() {
		VDomain r5 = VDomains.R(5);
		r5.isIn(Vector.of(-2f, 3f, 154f, 0f, 14f, 34f));
	}
	
	@Test
	public void test_R_N() {
		VDomain r5 = VDomains.R(5);

		Assert.assertTrue(r5.isIn(Vector.of(-2f, 3f, 154f, 0f, 1f)));
		Assert.assertFalse(r5.isIn(Vector.of(-2f, 3f, Float.NaN, 0f, 1f)));
		Assert.assertFalse(r5.isIn(Vector.of(-2f, 3f, Float.POSITIVE_INFINITY, 0f, 1f)));
		Assert.assertFalse(r5.isIn(Vector.of(-2f, 3f, Float.NEGATIVE_INFINITY, 0f, 1f)));
		
		Assert.assertFalse(r5.isEmpty());
		Assert.assertEquals("(]-∞, +∞[) X (]-∞, +∞[) X (]-∞, +∞[) X (]-∞, +∞[) X (]-∞, +∞[)", r5.toString());
	}
	
	@Test
	public void test_R_N_closed() {
		VDomain r5_closed = VDomains.R_closed(5);

		Assert.assertTrue(r5_closed.isIn(Vector.of(-2f, 3f, 154f, 0f, 1f)));
		Assert.assertTrue(r5_closed.isIn(Vector.of(-2f, 3f, Float.POSITIVE_INFINITY, 0f, 1f)));
		Assert.assertTrue(r5_closed.isIn(Vector.of(-2f, 3f, Float.NEGATIVE_INFINITY, 0f, 1f)));
		Assert.assertFalse(r5_closed.isIn(Vector.of(-2f, 3f, Float.NaN, 0f, 1f)));
		
		Assert.assertFalse(r5_closed.isEmpty());
		Assert.assertEquals("([-∞, +∞]) X ([-∞, +∞]) X ([-∞, +∞]) X ([-∞, +∞]) X ([-∞, +∞])", r5_closed.toString());
	}
	
	@Test
	public void test_Union() {
		VDomain r5 = VDomains.R_plus(5);
		VDomain union = r5.union(new Segment[] {new Segment(-2f, -1f)});

		Assert.assertTrue(union.isIn(Vector.of(0f, 0f, 0f, 0f, 0f)));
		Assert.assertTrue(union.isIn(Vector.of(-1.5f, -1.5f, -1.5f, -1.5f, -1.5f)));
		Assert.assertFalse(union.isIn(Vector.of(-1.5f, -1.5f, -2f, -1.5f, -1.5f)));
		Assert.assertFalse(union.isIn(Vector.of(-2, -2f, -2f, -2f, -2f)));
		
		Assert.assertFalse(union.isEmpty());
		Assert.assertEquals(
			"(]-2.0, -1.0[ ⋃ [0.0, +∞[) " 
			+ "X (]-2.0, -1.0[ ⋃ [0.0, +∞[) " 
			+ "X (]-2.0, -1.0[ ⋃ [0.0, +∞[) " 
			+ "X (]-2.0, -1.0[ ⋃ [0.0, +∞[) " 
			+ "X (]-2.0, -1.0[ ⋃ [0.0, +∞[)", 
			union.toString()
		);
	}
	
	@Test
	public void test_Inter() {
		VDomain r5 = VDomains.R_plus(5);
		VDomain inter = r5.inter(new Segment(-2f, 5f).open(true, false));

		Assert.assertTrue(inter.isIn(Vector.of(0f, 0f, 0f, 0f, 0f)));
		Assert.assertTrue(inter.isIn(Vector.of(0f, 0f, 0f, 5f, 0f)));
		Assert.assertTrue(inter.isIn(Vector.of(2f, 3f, 4f, 5f, 4f)));
		Assert.assertFalse(inter.isIn(Vector.of(2f, 3f, 4f, 5f, 5.00001f)));
		Assert.assertFalse(inter.isIn(Vector.of(0f, 0f, 0f, Float.MAX_VALUE * -1, 0f)));
		Assert.assertFalse(inter.isIn(Vector.of(-1.5f, -1.5f, -1.5f, -1.5f, -1.5f)));
		Assert.assertFalse(inter.isIn(Vector.of(-1.5f, 1.5f, 1.5f, 1.5f, 1.5f)));
		
		Assert.assertFalse(inter.isEmpty());
		Assert.assertEquals(
			"([0.0, 5.0]) X ([0.0, 5.0]) X ([0.0, 5.0]) X ([0.0, 5.0]) X ([0.0, 5.0])", 
			inter.toString()
		);
	}
	
	@Test
	public void test_Empty() {
		VDomain empty5 = VDomain.of(new Segment(0f, 0f).open(true, true), 5);
		Assert.assertTrue(empty5.isEmpty());
	}
	
	@Test
	public void test_compareTo() {
		Segment segment1 = new Segment(-2f, 5f).open(true, false);
		Segment segment2 = new Segment(-2f, -1f);

		VDomain empty5 = VDomain.of(new Segment(0f, 0f).open(true, true), 5);
		
		VDomain r4 = VDomains.R(4);
		VDomain r5 = VDomains.R(5);
		VDomain r5_plus = VDomains.R_plus(5);
		VDomain inter = r5_plus.inter(segment1);
		VDomain union = r5_plus.union(new Segment[] {segment2});
		VDomain r5_closed = VDomains.R_closed(5);
		
		TreeSet<Domain> domains = new TreeSet<>();
		domains.add(Domains.R);
		domains.add(r4);
		domains.add(r5);
		domains.add(empty5);
		domains.add(Domains.R_PLUS_STAR); 
		domains.add(segment1); 
		domains.add(segment2); 
		
		domains.add(r5_closed); 
		domains.add(r5_plus); 
		domains.add(inter); 
		domains.add(union);
		
		domains.add(VDomains.R_ANY);
		domains.add(VDomain.anyDimension(Domains.R_CLOSED));
		domains.add(VDomain.anyDimension(new Segment(-1f, -1f)));

		Assert.assertEquals(14, domains.size());
	}
	
	@Test
	public void test_R_anyDimension() {
		Assert.assertTrue(VDomains.R_ANY.isIn(Vector.of(0f)));
		Assert.assertTrue(VDomains.R_ANY.isIn(Vector.of(-5f, 4f)));
		Assert.assertTrue(VDomains.R_ANY.isIn(Vector.of(Float.MAX_VALUE, 0f, Float.MIN_VALUE)));
		Assert.assertEquals("(]-∞, +∞[)ⁿ", VDomains.R_ANY.toString());

		VDomain segmentN = VDomain.anyDimension(new Segment(0f, 1f).open(false, false));
		Assert.assertEquals("([0.0, 1.0])ⁿ", segmentN.toString());
		
		Assert.assertTrue(segmentN.isIn(Vector.of(0f)));
		Assert.assertTrue(segmentN.isIn(Vector.of(0f, 0f)));
		Assert.assertTrue(segmentN.isIn(Vector.of(0f, 1f)));
		Assert.assertTrue(segmentN.isIn(Vector.of(0f, 0.5f, 0.005f, 0f)));
		Assert.assertFalse(segmentN.isIn(Vector.of(0f, 0.5f, 1.001f, 0f)));
		Assert.assertFalse(segmentN.isIn(Vector.of(0f, 0.5f, 0.7f, -0.02f, 0.3f)));
	}
}
