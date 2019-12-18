package org.lappsgrid.eager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 *
 */
public class Test
{
	public void solve() {
		Scanner input = new Scanner(System.in);
		int N = input.nextInt();
		List<Integer> list = new ArrayList<>(N);
		list.add(input.nextInt());
		for (int i = 1; i < N; ++i) {
			int value = input.nextInt();
			int insertAt = Collections.binarySearch(list, value);
			int d = value - list.get(insertAt);
			if (d == 0) {
				System.out.println(0);
				return;
			}
		}

	}
	public void run()
	{
		Scanner input = new Scanner(System.in);
		int N = input.nextInt();
		int[] array = new int[N];
		array[0] = input.nextInt();
		int closest = Integer.MAX_VALUE;
		for (int end = 1; end < N; ++end)
		{
			int value = input.nextInt();

			// Find the insertion point for the value
			int current = end;
			int prev = current - 1;
			while (prev >= 0 && array[prev] > value)
			{
				int d = array[prev] - value;
				if (d < closest) closest = d;
				array[current] = array[prev];
				--current;
				--prev;
			}
			// Insert the value
			array[current] = value;
			// Keep checking for closest matches, but we only need to check values
			// that are within 'closest' points.
			while (prev > 0)
			{
				int d = value - array[prev];
				if (d < closest)
				{
					closest = d;
					--prev;
				} else
				{
					// Shortcircuit the search
					prev = 0;
				}
			}
			// System.err.printf("%d %d\n", i, array[i])
		}
		System.out.println(closest);
	}
/*
delta = Integer.MAX_VALUE
for (i = 0; i < N-1; ++i) {
    for (j = i+1; j < N; ++j) {
        int d = array[i] - array[j]
        if (d < 0) d = -d
        // System.err.printf("%d %d %d %d\n", i, j, d, delta)
        if (d < delta) {
            delta = d
        }
    }
}
*/
// Write an answer using println
// To debug: System.err << "Debug messages...\n"
// println closest	}
}
