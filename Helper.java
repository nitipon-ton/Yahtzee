public class Helper {
    public static double maxArr(double[] arr) {
		double max = 0;
		for (double d: arr) {
			max = Math.max(max, d);
		}
		return max;
	}
	public static int maxArr(int[] arr) {
		int max = 0;
		for (int i: arr) {
			if (i > max) {
				max = i;
			}
		}
		return max;
	}
	//trigger push
	public static int sumArr(int[] arr) {
		int sum = 0;
		for (int i: arr) {
			sum += i;
		}
		return sum;
	}
}
