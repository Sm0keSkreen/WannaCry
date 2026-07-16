package dev.mg.wannacry.util.inventory.strategy;

import dev.mg.wannacry.util.inventory.Result;

public interface SwapStrategy {
    boolean swap(Result result);

    boolean swapBack(int last, Result result);
}
