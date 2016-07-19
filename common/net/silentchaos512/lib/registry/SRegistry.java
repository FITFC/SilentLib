package net.silentchaos512.lib.registry;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import net.silentchaos512.lib.block.BlockContainerSL;
import net.silentchaos512.lib.block.BlockSL;
import net.silentchaos512.lib.item.ItemBlockSL;
import net.silentchaos512.lib.item.ItemSL;

public class SRegistry {

  private final Map<String, Block> blocks = Maps.newHashMap();
  private final Map<String, Item> items = Maps.newHashMap();
  private final List<IRegistryObject> registryObjects = Lists.newArrayList();

  public final String modId;
  public final String resourcePrefix;

  public SRegistry(String modId) {

    this.modId = modId;
    this.resourcePrefix = modId.toLowerCase() + ":";
  }

  public Block registerBlock(BlockSL block) {

    return registerBlock(block, block.getName());
  }

  public Block registerBlock(BlockContainerSL block) {

    return registerBlock(block, block.getName());
  }

  public Block registerBlock(Block block, String key) {

    return registerBlock(block, key, new ItemBlockSL(block));
  }

  public Block registerBlock(BlockSL block, ItemBlock itemBlock) {

    return registerBlock(block, block.getName(), itemBlock);
  }

  public Block registerBlock(BlockContainerSL block, ItemBlock itemBlock) {

    return registerBlock(block, block.getName(), itemBlock);
  }

  public Block registerBlock(Block block, String key, ItemBlock itemBlock) {

    blocks.put(key, block);
    if (block instanceof IRegistryObject) {
      registryObjects.add((IRegistryObject) block);
    }

    ResourceLocation resource = new ResourceLocation(resourcePrefix + key);
    block.setRegistryName(resource);
    GameRegistry.register(block);
    GameRegistry.register(itemBlock, resource);
    return block;
  }

  public Item registerItem(ItemSL item) {

    return registerItem(item, item.getName());
  }

  public Item registerItem(Item item, String key) {

    items.put(key, item);
    if (item instanceof IRegistryObject) {
      registryObjects.add((IRegistryObject) item);
    }

    ResourceLocation resource = new ResourceLocation(resourcePrefix + key);
    // item.setRegistryName(resource);
    GameRegistry.register(item, resource);
    return item;
  }

  public void registerTileEntity(Class<? extends TileEntity> tileClass, String key) {

    GameRegistry.registerTileEntity(tileClass, "tile." + resourcePrefix + key);
  }

  @SideOnly(Side.CLIENT)
  public <T extends TileEntity> void registerTileEntitySpecialRenderer(Class<T> tileClass,
      TileEntitySpecialRenderer<T> renderer) {

    ClientRegistry.bindTileEntitySpecialRenderer(tileClass, renderer);
  }

  @SideOnly(Side.CLIENT)
  public void registerEntityRenderer(Class<? extends Entity> entityClass, Render renderer) {

    Minecraft.getMinecraft().getRenderManager().entityRenderMap.put(entityClass, renderer);
  }

  public IRecipe addRecipeHandler(Class<? extends IRecipe> recipeClass, String name,
      Category category, String dependencies)
          throws InstantiationException, IllegalAccessException {

    IRecipe recipe = recipeClass.newInstance();
    GameRegistry.addRecipe(recipe);
    RecipeSorter.INSTANCE.register(resourcePrefix + name, recipeClass, category, dependencies);
    return recipe;
  }

  public Block getBlock(String key) {

    return blocks.get(key);
  }

  public Item getItem(String key) {

    return items.get(key);
  }

  public void preInit() {

    for (IRegistryObject obj : registryObjects)
      obj.addOreDict();
  }

  public void init() {

    for (IRegistryObject obj : registryObjects)
      obj.addRecipes();
  }

  public void postInit() {

  }

  public void clientPreInit() {

    registerModelVariants();
  }

  public void clientInit() {

    registerModels();
  }

  public void clientPostInit() {

  }

  /**
   * @deprecated Recipes and ore dictionary entries should be registered separately.
   */
  @Deprecated
  protected void addRecipesAndOreDictEntries() {

    for (IRegistryObject obj : registryObjects) {
      obj.addOreDict();
      obj.addRecipes();
    }
  }

  @SideOnly(Side.CLIENT)
  protected void registerModelVariants() {

    for (IRegistryObject obj : registryObjects) {
      Item item = obj instanceof Block ? Item.getItemFromBlock((Block) obj) : (Item) obj;
      List<ModelResourceLocation> models = obj.getVariants();
      // Remove nulls
      List<ModelResourceLocation> nonNullModels = Lists.newArrayList();
      for (ModelResourceLocation m : models) {
        if (m != null) {
          nonNullModels.add(m);
        }
      }

      ModelLoader.registerItemVariants(item,
          nonNullModels.toArray(new ModelResourceLocation[nonNullModels.size()]));

      // Custom mesh?
      // ItemMeshDefinition mesh = obj.getCustomMesh();
      // if (mesh != null) {
      // ModelLoader.setCustomMeshDefinition(item, mesh);
      // }
    }
  }

  @SideOnly(Side.CLIENT)
  protected void registerModels() {

    ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
    for (IRegistryObject obj : registryObjects) {
      if (!obj.registerModels()) {
        Item item = obj instanceof Block ? Item.getItemFromBlock((Block) obj) : (Item) obj;
        List<ModelResourceLocation> models = obj.getVariants();
        for (int i = 0; i < models.size(); ++i) {
          if (models.get(i) != null) {
            mesher.register(item, i, models.get(i));
          }
        }
      }
    }
  }
}
