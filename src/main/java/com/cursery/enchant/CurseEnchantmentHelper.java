package com.cursery.enchant;

import com.cursery.Cursery;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Apply random curses upon enchanting
 */
public class CurseEnchantmentHelper
{
    public static  boolean delayNext = false;
    public static  Item    delayItem;
    private static Random  rand      = new Random();

    public static ItemStack lastCursedItem = null;

    public static Map<Enchantment, Integer> curseWeightMap   = new HashMap<>();
    public static int                       totalCurseWeight = 0;

    public static ItemStack          notifyStack;
    public static ServerPlayerEntity notifyPlayer;

    /**
     * Checks the stack for applying a random curse
     *
     * @param stack       stack to check
     * @param previous    previous enchantments on the stack
     * @param newEnchants new enchantments on the stack
     * @return true if applied at least one curse
     */
    public static boolean checkForRandomCurse(final ItemStack stack, final Map<Enchantment, Integer> previous, final Map<Enchantment, Integer> newEnchants)
    {
        // Case for anvil repairs, we delay applying the curse till item is taken out
        if (delayNext)
        {
            delayNext = false;
            if (stack.getItem() == delayItem)
            {
                return false;
            }

            delayItem = null;
        }

        if (stack == null || stack.isEmpty() || previous == null || newEnchants == null)
        {
            return false;
        }

        // Same stack get same results
        rand.setSeed(System.identityHashCode(stack));
        lastCursedItem = null;

        int levelSum = 0;
        // Sum all levels
        for (final Map.Entry<Enchantment, Integer> newEnchant : newEnchants.entrySet())
        {
            if (!newEnchant.getKey().isCurse() && !(Cursery.config.getCommonConfig().excludeTreasure.get() && newEnchant.getKey().isTreasureOnly()))
            {
                levelSum += newEnchant.getValue();
            }
        }

        boolean appliedCurse = false;
        // Compare enchants
        for (final Map.Entry<Enchantment, Integer> newEnchant : newEnchants.entrySet())
        {
            int newLevel = previous.containsKey(newEnchant.getKey()) ? newEnchant.getValue() - previous.get(newEnchant.getKey()) : newEnchant.getValue();
            if (newLevel <= 0)
            {
                continue;
            }

            // Skip treasure enchants if configured
            if (Cursery.config.getCommonConfig().excludeTreasure.get() && newEnchant.getKey().isTreasureOnly())
            {
                continue;
            }

            if (!newEnchant.getKey().isCurse())
            {
                if (rollAndApplyCurseTo(stack, newLevel, levelSum - newLevel, newEnchants))
                {
                    appliedCurse = true;
                }
            }
        }

        // Remember it to allow notifying players, particles purple color and a text: The wheel of fortune turns. Ein hauch von schicksal. The dark etc
        if (appliedCurse)
        {
            lastCursedItem = stack;

            if (lastCursedItem == notifyStack && notifyPlayer != null)
            {
                PlayerVisualHelper.randomNotificationOnCurseApply(notifyPlayer, notifyStack);
                notifyPlayer = null;
                notifyStack = null;
            }
        }

        return appliedCurse;
    }

    /**
     * Rolls the curses and applies them, according to the total level and newly applied level of enchants
     *
     * @param stack       item
     * @param newLevel    additional enchant levels added
     * @param levelSum    total sum of existing enchants
     * @param newEnchants
     * @return
     */
    private static boolean rollAndApplyCurseTo(
      final ItemStack stack,
      final int newLevel,
      final int levelSum,
      final Map<Enchantment, Integer> newEnchants)
    {
        // Each level has the same chance, so its the same to apply enchant V vs I to V
        boolean appliedCurse = false;
        for (int i = 0; i < newLevel; i++)
        {
            if (rand.nextInt(100) < Math.min(75, Cursery.config.getCommonConfig().basecursechance.get() + levelSum - (stack.getItemEnchantability() >> 1)))
            {
                for (int j = 0; j < 15; j++)
                {
                    final Enchantment curse = getRandomCurse();
                    if (curse == null)
                    {
                        continue;
                    }

                    final int currentLevel = newEnchants.getOrDefault(curse, 0);
                    if (currentLevel < curse.getMaxLevel() && curse.canEnchant(stack) && isCompatibleWithAll(curse, newEnchants))
                    {
                        enchantManually(stack, curse, currentLevel + 1);
                        newEnchants.put(curse, currentLevel + 1);
                        appliedCurse = true;
                        break;
                    }
                }
            }
        }

        return appliedCurse;
    }

    /**
     * Check if an enchantment can be applied to the existing
     *
     * @param enchantment
     * @param newEnchants
     * @return true if possible
     */
    private static boolean isCompatibleWithAll(final Enchantment enchantment, final Map<Enchantment, Integer> newEnchants)
    {
        for (final Map.Entry<Enchantment, Integer> entry : newEnchants.entrySet())
        {
            if (!entry.getKey().isCompatibleWith(enchantment))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Get a weighted random curse
     *
     * @return
     */
    private static Enchantment getRandomCurse()
    {
        final int chosen = Cursery.rand.nextInt(totalCurseWeight);
        Enchantment curse = null;
        int currentWeight = 0;
        for (final Map.Entry<Enchantment, Integer> entry : curseWeightMap.entrySet())
        {
            if (chosen < entry.getValue() + currentWeight)
            {
                curse = entry.getKey();
                break;
            }
            currentWeight += entry.getValue();
        }

        return curse;
    }

    /**
     * NBT helper to apply the actual enchant, avoids recursive application
     *
     * @param stack
     * @param enchantment
     * @param level
     */
    public static void enchantManually(final ItemStack stack, Enchantment enchantment, int level)
    {
        stack.getOrCreateTag();
        if (!stack.getTag().contains("Enchantments", 9))
        {
            stack.getTag().put("Enchantments", new ListNBT());
        }

        ListNBT listnbt = stack.getTag().getList("Enchantments", 10);
        CompoundNBT compoundnbt = new CompoundNBT();
        compoundnbt.putString("id", String.valueOf((Object) Registry.ENCHANTMENT.getKey(enchantment)));
        compoundnbt.putShort("lvl", (short) ((byte) level));
        listnbt.add(compoundnbt);
    }

    /**
     * Weight calc, just vanilla now
     *
     * @param enchantment
     * @return weight
     */
    public static int calculateWeightFor(final Enchantment enchantment)
    {
        return enchantment.getRarity().getWeight();
    }
}