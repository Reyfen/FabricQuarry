package net.quarrymod.blockentity.machine.tier3;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.core.BlockPos;

public class MiningUtil {

    private MiningUtil() {
        // Hide constructor
    }

    /**
     * Creates an queue of positions that should be mined starting at the center of the position.
     * Positions are loaded in a circular pattern, for radius 2, this will be the loaded outcome.
     * 25 > 24  > 23  > 22 > 21
     *                        V
     * 10 >  9  >  8  >  7   20
     * ^                 V    V
     * 11    2  > [1]    6   19
     * ^     ^           V    V
     * 12    3  <  4  <  5   18
     * ^                      V
     * 13 < 14  < 15  < 16 < 17
     * @param radius to use around the position
     * @param miningPosition the position the center of the shaft.
     * @return A list of circular positions to mine.
     */
    public static Queue<BlockPos> createMiningPosition(int radius, BlockPos miningPosition) {
        LinkedList<BlockPos> minePositions = new LinkedList<>();

        // Start from negative radius
        int k = -radius;
        int l = -radius;
        // Add 1 to prevent off-by-one errors. (including end)
        int m = radius + 1;
        int n = radius + 1;

        //
        // https://www.geeksforgeeks.org/print-a-given-matrix-in-spiral-form/?ref=lbp
        // Explanation of the algorithm:
        // k - starting row index ( negative radius )
        // m - ending row index ( positive radius )
        // l - starting column index ( negative radius )
        // n - ending column index ( positive radius )
        //

        int i;
        while (k < m && l < n) {
            // Add the first row from the remaining rows
            for (i = l; i < n; ++i) {
                minePositions.addFirst(miningPosition.offset(k, 0, i));
            }
            k++;

            // Add the last column from the remaining columns
            for (i = k; i < m; ++i) {
                minePositions.addFirst(miningPosition.offset(i, 0, n - 1));
            }
            n--;

            // Add last remaining from the row */
            if (k < m) {
                for (i = n - 1; i >= l; --i) {
                    minePositions.addFirst(miningPosition.offset((m - 1), 0, i));
                }
                m--;
            }

            // Add the first column from the remaining columns */
            if (l < n) {
                for (i = m - 1; i >= k; --i) {
                    minePositions.addFirst(miningPosition.offset(i, 0, l));
                }
                l++;
            }
        }
        return minePositions;
    }


}
