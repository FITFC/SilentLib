package net.silentchaos512.lib.data.recipe.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.silentchaos512.lib.SilentLib;
import net.silentchaos512.lib.crafting.ingredient.ExclusionIngredient;
import net.silentchaos512.lib.data.recipe.ExtendedShapedRecipeBuilder;
import net.silentchaos512.lib.data.recipe.ExtendedSingleItemRecipeBuilder;
import net.silentchaos512.lib.data.recipe.LibRecipeProvider;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class TestRecipeProvider extends LibRecipeProvider {
    public TestRecipeProvider(DataGenerator generatorIn) {
        super(generatorIn, SilentLib.MOD_ID);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        SilentLib.LOGGER.fatal("Running test recipe provider! These files should NOT be included in release!");

        damageItemBuilder(Items.DIAMOND, 9)
                .damageToItems(3)
                .addIngredient(Items.DIAMOND_PICKAXE)
                .addIngredient(Blocks.DIAMOND_BLOCK)
                .addCriterion("has_item", has(Blocks.DIAMOND_BLOCK))
                .build(consumer, SilentLib.getId("damage_item_test1"));

        damageItemBuilder(Items.EMERALD, 9)
                .damageToItems(3)
                .addExtraData(json -> json.addProperty("test", "This is a test!"))
                .addIngredient(Items.DIAMOND_PICKAXE)
                .addIngredient(Blocks.EMERALD_BLOCK)
                .build(consumer, SilentLib.getId("damage_item_test2"));

        shapelessBuilder(Blocks.DIRT, 10)
                .addIngredient(Tags.Items.GEMS_EMERALD)
                .addExtraData(json -> json.addProperty("test2", "Can you hear me now?"))
                .build(consumer, SilentLib.getId("extended_shapeless_test1"));

        shapedBuilder(Items.DIAMOND_SWORD)
                .patternLine("  #")
                .patternLine(" # ")
                .patternLine("/  ")
                .key('#', Tags.Items.GEMS_DIAMOND)
                .key('/', Tags.Items.RODS_WOODEN)
                .addExtraData(json -> addLore(json, "Diagonal sword!", "<3 data generators"))
                .build(consumer, SilentLib.getId("extended_shaped_test1"));

        compressionRecipes(consumer, Items.EMERALD, Items.MAGMA_CREAM, Items.APPLE);

        smeltingAndBlastingRecipes(consumer, "reverse_glass_test", Tags.Items.GLASS_COLORLESS, Items.SAND, 0.625f);

        ExtendedSingleItemRecipeBuilder.stonecuttingBuilder(Ingredient.of(Items.COAL_BLOCK), Items.COAL, 9)
                .addExtraData(json -> json.addProperty("extra_test", "testing extra data!"))
                .build(consumer);

        ExtendedShapedRecipeBuilder.vanillaBuilder(Items.APPLE, 3)
                .key('#', ExclusionIngredient.of(ItemTags.PLANKS, Items.OAK_PLANKS))
                .patternLine("###")
                .patternLine("# #")
                .build(consumer);
    }

    private void addLore(JsonObject json, String... lore) {
        JsonObject result = GsonHelper.getAsJsonObject(json, "result");
        JsonObject display = new JsonObject();
        JsonObject nbt = new JsonObject();
        JsonArray array = new JsonArray();
        for (String line : lore) {
            array.add("\"" + line + "\"");
        }
        display.add("Lore", array);
        nbt.add("display", display);
        result.add("nbt", nbt);
    }
}
