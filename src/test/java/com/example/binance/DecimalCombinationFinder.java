package com.example.binance;

import java.util.ArrayList;
import java.util.List;

public class DecimalCombinationFinder {
    // 存储最终的组合结果
    private static List<List<Double>> result = new ArrayList<>();

    public static void main(String[] args) {
        // 示例：两位小数数组
        double[] decimalArray = {138, 94.8, 71.9, 150, 713, 91.8, 258.8, 258.8, 218.7};
        // 示例：目标两位小数
        double target = 1026.70;

        // 查找所有符合条件的组合
        findCombinations(decimalArray, target);

        // 输出结果
        if (result.isEmpty()) {
            System.out.println("没有找到能构成目标值 " + target + " 的组合");
        } else {
            System.out.println("能构成目标值 " + target + " 的组合有：");
            for (List<Double> combination : result) {
                System.out.println(combination);
            }
        }
    }

    /**
     * 查找数组中能构成目标值的所有组合
     *
     * @param array  两位小数数组
     * @param target 目标两位小数
     */
    public static void findCombinations(double[] array, double target) {
        // 清空上一次的结果
        result.clear();

        // 转换为整数（乘以100），避免浮点数精度问题
        int targetInt = (int) Math.round(target * 100);
        int[] intArray = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            // 四舍五入确保两位小数转换为整数无误差
            intArray[i] = (int) Math.round(array[i] * 100);
        }

        // 回溯查找组合
        backtrack(intArray, targetInt, 0, new ArrayList<>(), 0);
    }

    /**
     * 回溯算法核心方法
     *
     * @param intArray           转换后的整数数组
     * @param targetInt          转换后的目标整数
     * @param start              起始索引（避免重复组合）
     * @param currentCombination 当前组合（整数形式）
     * @param currentSum         当前组合的和（整数形式）
     */
    private static void backtrack(int[] intArray, int targetInt, int start, List<Integer> currentCombination, int currentSum) {
        // 找到符合条件的组合
        if (currentSum == targetInt) {
            // 转换回两位小数并加入结果集
            List<Double> decimalCombination = new ArrayList<>();
            for (int num : currentCombination) {
                decimalCombination.add(num / 100.0);
            }
            result.add(decimalCombination);
            return;
        }

        // 超过目标值，直接返回
        if (currentSum > targetInt) {
            return;
        }

        // 遍历数组，从start开始避免重复组合
        for (int i = start; i < intArray.length; i++) {
            // 选择当前元素
            currentCombination.add(intArray[i]);
            // 递归查找，起始索引为i+1（不重复使用同一元素）
            backtrack(intArray, targetInt, i + 1, currentCombination, currentSum + intArray[i]);
            // 回溯：移除最后一个元素，尝试下一个可能
            currentCombination.remove(currentCombination.size() - 1);
        }
    }
}
