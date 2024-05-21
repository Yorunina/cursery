package com.cursery.enchant;

import com.cursery.Cursery;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

import static com.cursery.Cursery.config;

/**
 * Apply random curses upon enchanting
 */
public class CurseEnchantmentHelper
{
    public static  boolean delayNext = false;
    public static  Item    delayItem;
    private static Random  rand      = new Random();

    public static Map<Enchantment, Integer> curseWeightMap   = new HashMap<>();
    public static int                       totalCurseWeight = 0;

    public static ItemStack                 notifyStack;
    public static ServerPlayer              notifyPlayer;
    public static Map<Enchantment, Integer> prevEnchants;

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
        ResourceLocation itemResource = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (Objects.nonNull(itemResource) && config.getCommonConfig().excludedItems.get().contains(itemResource.toString())) {
            return false;
        }

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

        if (stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK)
        {
            return false;
        }

        int levelSum = 0;
        // Sum all levels
        for (final Map.Entry<Enchantment, Integer> newEnchant : newEnchants.entrySet())
        {
            if (!newEnchant.getKey().isCurse() && !(config.getCommonConfig().excludeTreasure.get() && newEnchant.getKey().isTreasureOnly()))
            {
                levelSum += newEnchant.getValue();
            }
        }

        final List<Integer> addedLevels = new ArrayList<>();

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
            if (config.getCommonConfig().excludeTreasure.get() && newEnchant.getKey().isTreasureOnly())
            {
                continue;
            }

            if (!newEnchant.getKey().isCurse())
            {
                addedLevels.add(newLevel);
            }
        }

        for (final Integer newLevel : addedLevels)
        {
            if (rollAndApplyCurseTo(stack, newLevel, levelSum - newLevel, newEnchants))
            {
                appliedCurse = true;
            }
        }

        // Remember it to allow notifying players, particles purple color and a text: The wheel of fortune turns. Ein hauch von schicksal. The dark etc
        if (appliedCurse)
        {
            if (stack == notifyStack && notifyPlayer != null)
            {
                PlayerVisualHelper.randomNotificationOnCurseApply(notifyPlayer, notifyStack);
                notifyPlayer = null;
                notifyStack = null;
            }
        }
        else
        {
            if (stack == notifyStack && notifyPlayer != null)
            {
                PlayerVisualHelper.enchantSuccess(notifyPlayer, notifyStack);
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
            if (config.getCommonConfig().debugTries.get())
            {
                Cursery.LOGGER.info("Rolling new curse for " + stack + " addedEnchLevels: " + newLevel + " totalEnchantLevels: " + levelSum + " chance:" + Math.min(75,
                  config.getCommonConfig().basecursechance.get() + levelSum - (stack.getEnchantmentValue() >> 1)));
            }

            if (rand.nextInt(100) < Math.min(75, config.getCommonConfig().basecursechance.get() + levelSum - (stack.getEnchantmentValue() >> 1)))
            {
                if (config.getCommonConfig().debugTries.get())
                {
                    Cursery.LOGGER.info("Trying to apply curse to: " + stack);
                }

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
                        if (config.getCommonConfig().debugTries.get())
                        {
                            Cursery.LOGGER.info("Applying curse " + ForgeRegistries.ENCHANTMENTS.getKey(curse) + " to: " + stack);
                        }

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
                if (config.getCommonConfig().debugTries.get())
                {
                    Cursery.LOGGER.info(
                      "Curse " + ForgeRegistries.ENCHANTMENTS.getKey(enchantment) + " is not compatible with " + ForgeRegistries.ENCHANTMENTS.getKey(entry.getKey()));
                }

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
            stack.getTag().put("Enchantments", new ListTag());
        }

        ListTag listnbt = stack.getTag().getList("Enchantments", 10);
        CompoundTag compoundnbt = new CompoundTag();
        compoundnbt.putString("id", String.valueOf((Object) ForgeRegistries.ENCHANTMENTS.getKey(enchantment)));
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
