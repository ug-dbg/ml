package com.github.ugdbg.function.scalar.domain;

import com.github.ugdbg.function.domain.Domain;
import org.junit.Assert;
import org.junit.Test;

import static com.github.ugdbg.function.scalar.domain.Domains.*;

/**
 * Test case for the common {@link Domain} objects that can be found in {@link Domains}.
 */
public class DomainsTest {
	
	@Test
	public void testCommonDomains() {
		Assert.assertEquals("∅", new Segment(1F, 0F).toString());
		
		Assert.assertEquals("]-∞, +∞[",              R.toString());
		Assert.assertEquals("[-∞, +∞]",              R_CLOSED.toString());
		Assert.assertEquals("[0.0, +∞[",             R_PLUS.toString());
		Assert.assertEquals("]-∞, 0.0]",             R_MINUS.toString());
		Assert.assertEquals("]0.0, +∞[",             R_PLUS_STAR.toString());
		Assert.assertEquals("]-∞, 0.0[",             R_MINUS_STAR.toString());
		Assert.assertEquals("]-∞, 0.0[ ⋃ ]0.0, +∞[", R_STAR.toString());
		Assert.assertEquals("]0.0, 0.0[",            R_MINUS_STAR.inter(R_PLUS_STAR).toString());

		Assert.assertFalse("0 ∈ " + R_MINUS_STAR, R_MINUS_STAR.isIn(0F));
		
		Domain zero = R_MINUS_STAR.inter(R_PLUS_STAR);
		Assert.assertFalse("0 ∈ " + zero, zero.isIn(0F));
		Assert.assertFalse("7 ∈ " + zero, zero.isIn(7F));
		Assert.assertTrue("7 ∈ " + R_STAR, R_STAR.isIn(7F));
		Assert.assertFalse("0 ∈ " + R_STAR, R_STAR.isIn(0F));

		Domain inter = new Segment(-5f, 9f).open(false, false).inter(
			new Segment(-5f, 9f).open(false, false),
			new Segment(-5f, 7f).open(false, true),
			new Segment(-3f, 9f).open(true, false),
			new Segment(-10f, 9f).open(false, false)
		);

		Domain union = new Segment(-5f, 9f).open(false, false).union(
			new Segment(-5f, 9f).open(false, false),
			new Segment(-5f, 7f).open(false, true),
			new Segment(-3f, 9f).open(true, false),
			new Segment(-10f, 9f).open(false, false)
		);
		
		Assert.assertFalse("⋂ " + inter  + " is empty", inter.isEmpty());
		Assert.assertFalse(union + " is empty", inter.isEmpty());

		union = new Union(R_MINUS_STAR, R_PLUS_STAR).inter(new Segment(0f, 0f));
		Assert.assertFalse(union + " is empty", union.isEmpty());
	}
	
}
