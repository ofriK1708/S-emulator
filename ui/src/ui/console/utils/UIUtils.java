package ui.console.utils;

import engine.utils.ProgramUtils;

import java.util.*;

public class UIUtils
{
    public static final Comparator<String> programNameComparator =
            Comparator.comparingInt(str -> Integer.parseInt(str.substring(1)));
    private static final Scanner scanner = new Scanner(System.in);
    private static final String invalidChoiceFormat = "Invalid choice. please enter a number between %d and %d.%n";

    public static void getUserArguments(Set<String> programArgsName, List<Integer> arguments)
    {
        System.out.println("Please enter the program arguments (non-negative numbers separated by commas):");
        List<String> ProgramArgs = programArgsName.stream()
                .sorted(programNameComparator)
                .toList();
        System.out.println(String.join(", ", ProgramArgs));
        boolean valid = false;
        String userArguments = scanner.nextLine();
        while (!valid)
        {
            try
            {
                arguments.clear();
                for (String arg : userArguments.split(","))
                {
                    int progArg = Integer.parseInt(arg.trim());
                    arguments.add(progArg);
                }
                valid = arguments.stream().allMatch(value -> value >= 0);
                if (!valid)
                {
                    System.out.println("Invalid input! Please enter only non-negative integers separated by commas.");
                    userArguments = scanner.nextLine();
                }
            } catch (NumberFormatException e)
            {
                System.out.println("Invalid input! Please enter only non-negative integers separated by commas.");
                userArguments = scanner.nextLine();
            }
        }
        System.out.println("Arguments loaded successfully.");
    }

    public static void printSortedMap(Map<String, Integer> map)
    {
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(programNameComparator))
                .forEach(entry -> System.out.print(" " + entry.getKey() + " = " + entry.getValue()));
        System.out.println();
    }

    public static void printSortedSet(Set<String> set)
    {
        boolean ExitAppeared = false;
        if (set.contains(ProgramUtils.EXITLabelName))
        {
            ExitAppeared = true;
            set.remove(ProgramUtils.EXITLabelName);
        }
        List<String> sortedArgs = new ArrayList<>(set.stream()
                .sorted(programNameComparator)
                .toList());
        if (ExitAppeared)
        {
            sortedArgs.addLast(ProgramUtils.EXITLabelName);
        }
        System.out.println(String.join(", ", sortedArgs));
    }

    public static int getUserChoice(int maxOption)
    {
        while (true)
        {
            try
            {
                System.out.printf("Enter an option (1-%d): ", maxOption);
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice >= 1 && choice <= maxOption)
                {
                    return choice;
                }
                System.out.printf(invalidChoiceFormat, 1, maxOption);
            } catch (IllegalArgumentException e)
            {
                System.out.printf(invalidChoiceFormat, 1, maxOption);
            }
        }
    }

    public static int getExpandLevelChoiceFromUser(int maxLevel)
    {
        System.out.println("Please enter the expand level you would like to expand:");
        System.out.println("Please enter a number between 0 and " + maxLevel);
        while (true)
        {
            try
            {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 0 && choice <= maxLevel)
                {
                    return choice;
                }
                System.out.printf(invalidChoiceFormat, 0, maxLevel);
            } catch (IllegalArgumentException e)
            {
                System.out.printf(invalidChoiceFormat, 0, maxLevel);
            }
        }
    }
}
