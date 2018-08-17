/*
 * SilentLib - StackHelper
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.lib.util;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.silentchaos512.lib.collection.StackList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class StackHelper {
    /**
     * Creates an {@link ItemStack} from the block or item. Returns an empty stack if {@code
     * blockOrItem} is not a block or item.
     *
     * @param blockOrItem A block or an item
     * @return A stack of one of the block or item, or an empty stack if the object is not a block
     * or item
     * @since 2.3.17
     */
    public static ItemStack fromBlockOrItem(IForgeRegistryEntry<?> blockOrItem) {
        if (blockOrItem instanceof Block)
            return new ItemStack((Block) blockOrItem);
        else if (blockOrItem instanceof Item)
            return new ItemStack((Item) blockOrItem);
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static ItemStack loadFromNBT(@Nullable NBTTagCompound tags) {
        return tags != null ? new ItemStack(tags) : ItemStack.EMPTY;
    }

    /**
     * Gets the NBT tag compound for the stack.
     *
     * @param stack        the {@code ItemStack}
     * @param createIfNull if true, a new, empty {@code NBTTagCompound} will be set on the stack if
     *                     it does not have one
     * @return The stack's tag compound, or {@code null} if it does not have one and {@code
     * createIfNull} is false
     */
    @Deprecated
    public static NBTTagCompound getTagCompound(@Nonnull ItemStack stack, boolean createIfNull) {
        if (stack.isEmpty())
            return null;
        if (!stack.hasTagCompound() && createIfNull)
            stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    @Nonnull
    public static List<ItemStack> getOres(String oreDictKey) {
        return OreDictionary.getOres(oreDictKey);
    }

    @Nonnull
    public static List<ItemStack> getOres(String oreDictKey, boolean alwaysCreateEntry) {
        return OreDictionary.getOres(oreDictKey, alwaysCreateEntry);
    }

    /**
     * Gets all ore dictionary keys for the stack. If the stack is empty, an empty list is
     * returned.
     *
     * @param stack The ItemStack, which may be empty.
     * @return A list of strings, which may be empty.
     * @since 2.3.2
     */
    @Nonnull
    public static List<String> getOreNames(@Nonnull ItemStack stack) {
        List<String> list = new ArrayList<>();
        if (stack.isEmpty())
            return list;

        for (int id : OreDictionary.getOreIDs(stack))
            list.add(OreDictionary.getOreName(id));
        return list;
    }

    public static boolean matchesOreDict(ItemStack stack, String oreDictKey) {
        if (stack.isEmpty())
            return false;

        for (String oreName : getOreNames(stack))
            if (oreName.equals(oreDictKey))
                return true;

        for (ItemStack stackOre : getOres(oreDictKey))
            if (stack.isItemEqual(stackOre))
                return true;

        return false;
    }

    /**
     * Gets all non-empty stacks inside the inventory. Inventories can be painful to iterate over,
     * so this takes care of the ugly part.
     *
     * @param inv The inventory
     * @return A StackList of non-empty ItemStacks
     * @since 3.0.0(?)
     */
    public static StackList getNonEmptyStacks(@Nonnull IInventory inv) {
        StackList list = StackList.of();
        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            // StackList automatically filters empty stacks
            list.add(inv.getStackInSlot(i));
        }
        return list;
    }
}
