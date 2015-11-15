package lfsom.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTimeCalcNeigh {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		double distancia = 5;
		double opt1 = 1;
		double learnrate = 0.7;
		double result = 0;
		double a = 0;
		long startTime = System.currentTimeMillis();
		for (int k = 1; k < 4898; k++) {
			result = learnrate * Math.exp(-1 * distancia / (opt1 + 1 / k));
			System.out.println(result);
			a += result / k;
		}
		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println(totalTime + " " + a);
	}

}
