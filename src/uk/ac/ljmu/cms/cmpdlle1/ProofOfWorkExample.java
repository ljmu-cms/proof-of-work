package uk.ac.ljmu.cms.cmpdlle1;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * Application for experimenting with proof-of-work algorithms.
 * In this case, hashes of incrementing values are searched for
 * a result that has a given number of initial zeroes in its binary
 * representation
 * 
 * Liverpool John Moores University
 * 4th March 2014
 * Licensed under the GPL v.3
 * 
 * @author David Llewellyn-Jones, D.Llewelyly-Jones@ljmu.ac.uk
 *
 */
public class ProofOfWorkExample {
	/**
	 * Ensure we have a Scanner to read input from the console
	 */
	static Scanner console = new Scanner(System.in);

	/**
	 * Entry point for the program. Call interactiveSearch() or measureSearchTime()
	 * depending on what you want the application to do.
	 * 
	 * @param args - ignored
	 */
	public static void main(String[] args) {
		try {
			// Comment/uncomment the function to call
			
			// Get string and number of initial zeroes to hash from the user
			interactiveSearch();
			
			// Calculate average search times for different numbers of initial zeroes
			//measureSearchTime();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("SHA-256 hash algorithm isn't implemented on this system");
		} catch (UnsupportedEncodingException e) {
			System.out.println("UTF-8 string encoding isn't supported on this system");
		}
	}

	/**
	 * Request a string of text from the user and the number of bytes to find. The string
	 * is represented as a number made up from the sequence of bytes of the hash.
	 * The function will then show the first hash value that has the requested number
	 * of initial zeroes, with the input to the hash incremented by 1 each time.
	 * 
	 * @throws NoSuchAlgorithmException - thrown if the system doesn't support SHA-256
	 * @throws UnsupportedEncodingException - thrown if the system doesn't support string conversion to UTF-8
	 */
	public static void interactiveSearch() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		// Get the parameters from the user
		System.out.println("Enter the text to hash");
		String textToHash = console.nextLine();
		System.out.println("How many zero bits do you want to search for?");
		int zeroBitsNeeded = console.nextInt();
		System.out.println("Searching...");
		
		boolean foundZeroes = false;
		BigInteger increment = BigInteger.ZERO;
		// Convert the string to bytes
		byte[] bytesToHash = textToHash.getBytes("UTF-8"); 

		// Search through until we find the right number of initial zeroes
		while (!foundZeroes) {
			// Convert the string to a hash value
			md.reset();
			md.update(bytesToHash);
			byte[] digest = md.digest();
			
			// Count the number of initial zeroes
			int zeroes = countZeroBits(digest);
			
			// Check whether this is enough
			if (zeroes >= zeroBitsNeeded) {
				// Yes it is! So output the results
				System.out.println("Found solution:\n" + convertBytesToHexString(digest));
				System.out.println("Binary representation:" + convertBytesToBinaryString(digest));
				System.out.println("Increment was: " + increment.toString());
				foundZeroes = true;
			}
			// Increment the value to be hashed by one
			increment = increment.add(BigInteger.ONE);
			incrementBytes(bytesToHash);
		}
	}

	/**
	 * Perform a sequence of tests to establish a rough average time
	 * required to find hash values with increasing numbers of initial
	 * zeroes. Results are output to the console.
	 * 
	 * @throws NoSuchAlgorithmException - thrown if the system doesn't support SHA-256
	 * @throws UnsupportedEncodingException - thrown if the system doesn't support string conversion to UTF-8
	 */
	public static void measureSearchTime() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		int zeroBitsNeeded = 0;
		int howManyToFind = 1 << 25;
		String textToHash = "All you need is love";
		
		// Work through the number of initial zeroes
		while (zeroBitsNeeded <= 256) {
			System.out.println("\nZeroes: " + zeroBitsNeeded);
			int found = 0;
			BigInteger increment = BigInteger.ZERO;
			byte[] bytesToHash = textToHash.getBytes("UTF-8");
			
			// Get the initial time so we can calculate how long the process takes
			long start = System.currentTimeMillis();
			
			// We need to find a few so we can calculate an average
			while (found < howManyToFind) {
				// Convert the string to a hash value
				md.reset();
				md.update(bytesToHash);
				byte[] digest = md.digest();
				
				// Count the number of initial zeroes
				int zeroes = countZeroBits(digest);
				
				// Check whether this is enough
				if (zeroes >= zeroBitsNeeded) {
					// Yes it is!
					found++;
				}
				// Increment the value to be hashed by one
				increment = increment.add(BigInteger.ONE);
				incrementBytes(bytesToHash);
			}
			// Get the final time so we can calculate how long the process took
			long end = System.currentTimeMillis();
			long timeTaken = end - start;
			
			// Output the results
			System.out.println("Found: " + found);
			System.out.println("Time taken: " + timeTaken);
			if (timeTaken > 0) {
				double average = (double)timeTaken / (double)found;
				System.out.println("Average: " + average);
			}
			
			// Move on to the next test
			zeroBitsNeeded++;
			howManyToFind >>= 1;
			if (howManyToFind <= 5) {
				howManyToFind = 5;
			}
		}
	}
	
	/**
	 * Helper function to convert an array of bytes into a string in hexadecimal representation
	 * Useful for output the values to the screen
	 * 
	 * @param values - array of bytes to be converted
	 * @return string representing value in hexadecimal representation
	 */
	static String convertBytesToHexString(byte[] values) {
		// Start with an empty string
		String output = "";
		// Move through all of the bytes
		for (int bytePos = 0; bytePos < values.length; bytePos++) {
			// Convert the byte to a hash value and prepend it to the string
			output = String.format("%02x", (values[bytePos] & 0xff)) + output;
			// Add some gaps between pairs of bytes
			if (bytePos % 1 == 0) {
				output = " " + output;
			}
		}
		
		return output;
	}
	
	/**
	 * Helper function to convert an array of bytes into a string in binary representation
	 * Useful for output the values to the screen
	 * 
	 * @param values - array of bytes to be converted
	 * @return string representing value in binary representation
	 */
	static String convertBytesToBinaryString(byte[] values) {
		// Start with an empty string
		String output = "";
		// Move through all of the bytes
		for (int bytePos = 0; bytePos < values.length; bytePos++) {
			// Move through all of the bits in the byte
			int shift = values[bytePos];
			for (int bitPos = 0; bitPos < 8; bitPos++) {
				// Test whether this bit is zero or one
				if ((shift & 1) == 0) {
					// Prepend the appropriate value to the string
					output = "0" + output;
				}
				else {
					// Prepend the appropriate value to the string
					output = "1" + output;
				}
				// Move on to the next bit
				shift >>= 1;
			}
			// Add a gap between each byte
			output = " " + output;
			// Add a newline between every eight bytes
			if (bytePos % 8 == 7) {
				output = "\n" + output;
			}
		}
		
		return output;
	}
	
	/**
	 * Helper function to count the number of initial zeros a sequence
	 * of bit has. The proof-of-work algorithm asks a hash to be found
	 * with a certain number of initial zeros. The more zeroes requested
	 * the longer the search is likely to take.
	 * 
	 * @param values - array representing the number as a sequence of bytes, first value is least significant byte
	 * @return the number of initial zeroes
	 */
	static int countZeroBits(byte[] values) {
		int zeroes = 0;
		boolean more = true;
		// Move from LSByte upwards
		for (int bytePos = 0; more && (bytePos < values.length); bytePos++) {
			int shift = values[bytePos];
			// Move from LSB upwards counting until we hit a 1
			for (int bitPos = 0; more && (bitPos < 8); bitPos++) {
				// Test whether this bit is zero or one
				if ((shift & 1) == 0) {
					// This is zero, so increment the count
					zeroes++;
				}
				else {
					// Not a zero, so we're done
					more = false;
				}
				// Move on to the next bit
				shift >>= 1;
			}
		}
		
		return zeroes;
	}
	
	/**
	 * Helper function to increment a number by 1, when 
	 * the number is stored as a sequence of bytes
	 * 
	 * @param values - the number stored as a sequence of bytes, first value is least significant byte
	 */
	static void incrementBytes (byte[] values) {
		boolean overflow = true;
		int bytePos = 0;
		// Move through each of the bytes until there's no overflow
		while (overflow && (bytePos < values.length)) {
			// Check whether adding one will cause an overflow to the next byte
			if ((values[bytePos] & 0xff) < 255) {
				// No overflow, so just add one to this byte
				values[bytePos]++;
				overflow = false;
			}
			else {
				// There's an overflow, so cycle back to zero ready to start incrementing the next byte
				values[bytePos] = 0;
			}
			// Move on to the next byte
			bytePos++;
		}
	}
	
}
