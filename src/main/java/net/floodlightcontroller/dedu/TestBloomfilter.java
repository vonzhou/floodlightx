package net.floodlightcontroller.dedu;

public class TestBloomfilter {
	private void mian() {
		double falsePositiveProbability = 0.1;
		int expectedSize = 100;
		
		BloomFilter<String> bloomfilter = new BloomFilter<String>(falsePositiveProbability, expectedSize);
		bloomfilter.add("hello");
		for(int i = 0; i < 40; i++){
			bloomfilter.add("vonzhou" + i);
		}
		System.out.println("Bloom filter if contains hello" + bloomfilter.contains("hello"));
	}

}
