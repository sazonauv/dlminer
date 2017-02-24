package io.dlminer.learn;

import io.dlminer.print.Out;

import java.util.Arrays;


public class KMeans {
	
	public static final String RANDOM_MEANS = "RANDOM_MEANS";
	public static final String RANDOM_ASSIGNMENTS = "RANDOM_ASSIGNMENTS";
	public static final String RANDOM_SPREAD = "RANDOM_SPREAD";
	
	public static final String DIST_EUCLID = "DIST_EUCLID";
	public static final String DIST_OWA = "DIST_OWA";
	
	public static final String ERR_INIT = "Wrong initialization method";
	public static final String ERR_DIST = "Wrong distance metric";
	public static final String ERR_CLUSTER = "Wrong cluster choice";
	
	private int stop;

	private double[][] points;
	private int[] assignments;
	
	private double[][] clusterMeans;
	private int[] clusterSizes;
	
	private int nChanges;
	
	private String initMethod;
	private String distanceMetric;	
	
	public KMeans(double[][] points, int stop, String initMethod, String distanceMetric) {
		initAttributes(points, stop, initMethod, distanceMetric);
	}
	
	
	private void initAttributes(double[][] points, int stop, String initMethod, String distanceMetric) {
		this.points = points;
		this.stop = stop;
		this.initMethod = initMethod;
		this.distanceMetric = distanceMetric;
		assignments = new int[points.length];
	}
	

	private void initClusters(int nClusters) {
		clusterMeans = new double[nClusters][points[0].length];
		clusterSizes = new int[nClusters];
		if (initMethod.equals(RANDOM_ASSIGNMENTS)) {
			initRandomAssignments();
		} else if (initMethod.equals(RANDOM_MEANS)) {
			initRandomMeans();
		} else if (initMethod.equals(RANDOM_SPREAD)) {
			initRandomSpreads();
		} else {
			throw new IllegalArgumentException(ERR_INIT);
		}
	}
	
	
	private void initRandomMeans() {
		for (int i=0; i<clusterMeans.length; i++) {
			int meanInd = (int)(Math.random()*points.length);
			for (int j=0; j<clusterMeans[0].length; j++) {
				clusterMeans[i][j] = points[meanInd][j];			
			}
		}
	}
	
	private void initRandomAssignments() {
		for (int i=0; i<assignments.length; i++) {
			assignments[i] = (int)(Math.random()*clusterMeans.length);
		}
		update();
	}
	
	private void initRandomSpreads() {
		double[] distances = new double[points.length];
		// choose one centre randomly
		int meanInd = (int)(Math.random()*points.length);
		for (int j=0; j<clusterMeans[0].length; j++) {
			clusterMeans[0][j] = points[meanInd][j];
		}
		// choose other centres proportionally to the distances
		for (int i=1; i<clusterMeans.length; i++) {
			// calculate distances to the closest centre
			calcDistances(i, distances);
			// choose a new centre based on the distances
			int choice = chooseCentre(distances);
			if (choice < 0) {
				throw new NullPointerException(ERR_CLUSTER);
			}
			for (int j=0; j<clusterMeans[0].length; j++) {
				clusterMeans[i][j] = points[choice][j];
			}
		}		
	}
	
	private void calcDistances(int upToCluster, double[] distances) {
		for (int j=0; j<distances.length; j++) {
			double min = Double.MAX_VALUE;
			for (int k=0; k<upToCluster; k++) {
				double dist = dist(points[j], clusterMeans[k]);
				if (min > dist) {
					min = dist;						
				}
			}
			distances[j] = min;
		}
	}
	
	// ~ d
	// ~ d*d
	private int chooseCentre(double[] distances) {
		double rand = Math.random();
		double sum = 0;			
		for (int j=0; j<distances.length; j++) {
			sum += distances[j]*distances[j];
		}
		int choice = -1;
		if (sum == 0) {
			choice = (int) rand*distances.length;
		} else {
			double currSum = 0;		
			for (int j=0; j<distances.length; j++) {
				currSum += distances[j]*distances[j];
				if (currSum/sum > rand) {
					choice = j;
					break;
				}
			}
		}
		return choice;
	}
	
	
	private void initMultipleRuns(int nClusters, int times) {
		clusterMeans = new double[nClusters][points[0].length];
		clusterSizes = new int[nClusters];		
		// collect data from multiple runs
		double[][] centroids = new double[times*nClusters][];
		for (int i=0; i<times; i++) {
			KMeans kmeans = new KMeans(points, stop, initMethod, distanceMetric);
			kmeans.cluster(nClusters);
			double[][] means = kmeans.getClusterMeans();
			for (int j=0; j<means.length; j++) {
				centroids[i*nClusters+j] = means[j];
			}
		}
		// init clusters
		KMeans kmeans = new KMeans(centroids, stop, initMethod, distanceMetric);
		kmeans.cluster(nClusters);		
		double[][] means = kmeans.getClusterMeans();
		for (int i=0; i<means.length; i++) {
			for (int j=0; j<means[0].length; j++) {
				clusterMeans[i][j] = means[i][j];
			}
		}
	}

	
	public void cluster(int nClusters, int times) {
		initMultipleRuns(nClusters, times);
		run();
	}
	

	public void cluster(int nClusters) {
		initClusters(nClusters);
		run();
	}
	
	
	private void run() {
//		p("\nInitial means:");
//		printArray(points);
		do {
			assign();
			update();
			// stopping criteria
		} while (nChanges > stop);
	}
	
	
	public int[][] clusterHierarchy(int min, int step, int max, int times) {
		int nLevels = (max - min)/step + 1;
		int[][] hierarchy = new int[nLevels][assignments.length];
		int i = 0;
		for (int cl=min; cl<=max; cl+=step) {
			KMeans kmeans = new KMeans(points, stop, initMethod, distanceMetric);
			kmeans.cluster(cl, times);			
			hierarchy[i] = kmeans.getAssignments();
			Out.p("\nCluster means:");
			printArray(kmeans.getClusterMeans());
			i++;
		}		
		return hierarchy;
	}


	private void assign() {
		nChanges = 0;
		// assign clusters
		for (int i=0; i<points.length; i++) {			
			int initCl = assignments[i];
			double min = Double.MAX_VALUE;
			for (int j=0; j<clusterMeans.length; j++) {
				double dist = dist(points[i], clusterMeans[j]);
				if (min > dist) {
					min = dist;
					assignments[i] = j;
				}
			}
			if (assignments[i] != initCl) {
				nChanges++;
			}
		}
	}
	
	private void update() {		
		// update means
		if (distanceMetric.equals(DIST_EUCLID)) {
			updateMeansEuclid();
		} else if (distanceMetric.equals(DIST_OWA)) {
			updateMeansOWA();
		}
	}
	
	
	private void updateMeansEuclid() {
		// clear means
		clearMeans();
		// sum up
		for (int i=0; i<points.length; i++) {
			int cl = assignments[i];
			clusterSizes[cl]++;
			for (int j=0; j<clusterMeans[0].length; j++) {
				clusterMeans[cl][j] += points[i][j];
			}
		}
		// normalise
		normMeans();
	}
	
	
	private void updateMeansOWA() {
		updateMeansEuclid();		
	}
	
	
	private void roundMeans() {
		for (int i=0; i<clusterMeans.length; i++) {
			if (clusterSizes[i] > 0) {
				for (int j=0; j<clusterMeans[0].length; j++) {
					clusterMeans[i][j] = Math.round(clusterMeans[i][j]);
				}
			}
		}
	}
	
		
	
	private void normMeans() {
		for (int i=0; i<clusterMeans.length; i++) {
			if (clusterSizes[i] > 0) {
				for (int j=0; j<clusterMeans[0].length; j++) {
					clusterMeans[i][j] /= clusterSizes[i];
				}
			}
		}
	}
	
	private void clearMeans() {
		Arrays.fill(clusterSizes, 0);
		for (int i=0; i<clusterMeans.length; i++) {			
			Arrays.fill(clusterMeans[i], 0);
		}
	}
			
	
	private double dist(double[] x, double[] mean) {
		Double dist = null;
		if (distanceMetric.equals(DIST_EUCLID)) {
			dist = distEuclid(x, mean);
		} else if (distanceMetric.equals(DIST_OWA)) {
			dist = distOWA(x, mean);
		}
		return dist;
	}

	private double distEuclid(double[] x, double[] mean) {
		double sum = 0;
		for (int i=0; i<x.length; i++) {
			sum += (x[i]-mean[i])*(x[i]-mean[i]);
		}
		return Math.sqrt(sum);
	}
	
	private double distOWA(double[] x, double[] mean) {		
		double sum = 0;
		int count = 0;
		for (int i=0; i<x.length; i++) {	
			if (x[i] == 1) {				
				sum += x[i] - mean[i];
				count++;
			}			
		}
		sum = (count == 0) ? 1 : sum/count;
		return sum;
	}

	public int[] getAssignments() {
		return assignments;
	}	
	
	public double[][] getClusterMeans() {
		return clusterMeans;
	}


//	private static void p(Object o) {
//		Out.p(o);
//	}
	
	private static void printArray(double[][] arr) {
		for (int i=0; i<arr.length; i++) {
			Out.p(Arrays.toString(arr[i]));
		}
	}
	
	private static double findOverlap(int[] arr1, int[] arr2) {
		double overlap = 0;
		for (int i=0; i<arr1.length; i++) {			
			for (int j=0; j<arr2.length; j++) {
				if (arr1[i] == arr2[j]) {
					overlap++;
					break;
				}
			}			
		}
		return overlap/arr1.length;
	}

	
	public static void main(String... args) {
		// init points
		int nCls = 10;
		int nPointsInCl = 100;
		int dim = 5;
		double spread = 10;
		double[][] points = new double[nCls*nPointsInCl][dim];
		for (int i=0; i<nCls; i++) {
			for (int j=0; j<nPointsInCl; j++) {
				for (int d=0; d<dim; d++) {
//					points[i*nPointsInCl+j][d] = i*spread + Math.random();
//					points[i*nPointsInCl+j][d] = Math.random() >= 0.5 ? 1 : 0;
					points[i*nPointsInCl+j][d] = (i % (d+1) == 0) ? 1 : 0;
				}
			}
		}
		Out.p("Points:");
		printArray(points);
		// init k-means
		int stop = 10;
		int runs = 100;
		// hierarchical clustering		
		int min = 2;
		int max = nCls;
		int step = 2;
		KMeans kmeansH = new KMeans(points, stop, RANDOM_SPREAD, DIST_EUCLID);
		int[][] hier = kmeansH.clusterHierarchy(min, step, max, runs);
		
	}
	
}
